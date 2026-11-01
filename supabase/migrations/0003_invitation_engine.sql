-- ==============================================================================
-- MAHFAZA.CO V34.2 — SECURE INVITATION ENGINE & ATOMIC REGISTRATION
-- Strict Invitation-Only Policy, Anti-Race Conditions & Zero-Uninvited Accounts
-- File: supabase/migrations/0003_invitation_engine.sql
-- ==============================================================================

-- 1. INVITATIONS TABLE
CREATE TABLE IF NOT EXISTS public.invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code_hash VARCHAR(64) NOT NULL, -- SHA-256 of plain text token
    role user_role NOT NULL DEFAULT 'STUDENT',
    assigned_coach_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    used_by UUID REFERENCES auth.users(id) ON DELETE SET NULL, -- References auth.users so AFTER INSERT trigger has valid FK
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    revoked_at TIMESTAMPTZ,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Partial index ensuring code hash uniqueness among non-revoked and unused codes
CREATE UNIQUE INDEX IF NOT EXISTS uq_invitations_code_hash 
ON public.invitations(code_hash) 
WHERE used_at IS NULL AND revoked_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_invitations_coach ON public.invitations(assigned_coach_id);
CREATE INDEX IF NOT EXISTS idx_invitations_status ON public.invitations(expires_at, used_at, revoked_at);

-- 2. ENABLE RLS ON INVITATIONS
ALTER TABLE public.invitations ENABLE ROW LEVEL SECURITY;

CREATE POLICY "invitations_admin_all" ON public.invitations
FOR ALL USING (public.is_admin());

CREATE POLICY "invitations_coach_view" ON public.invitations
FOR SELECT USING (
    public.is_coach() AND (created_by = auth.uid() OR assigned_coach_id = auth.uid())
);

-- ==============================================================================
-- 3. SECURE CODE GENERATION FUNCTION (Admin or Coach only)
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.create_invitation(
    p_role user_role DEFAULT 'STUDENT',
    p_assigned_coach_id UUID DEFAULT NULL,
    p_valid_days INTEGER DEFAULT 7,
    p_metadata JSONB DEFAULT '{}'::jsonb
)
RETURNS TABLE(invitation_id UUID, plain_code TEXT, expires_at TIMESTAMPTZ) AS $$
DECLARE
    v_raw_code TEXT;
    v_code_hash TEXT;
    v_expires_at TIMESTAMPTZ;
    v_creator_id UUID;
    v_new_id UUID;
BEGIN
    v_creator_id := auth.uid();
    
    -- Privilege check: Only Admin can create COACH or ADMIN invitations
    -- Coach can only create STUDENT invitations assigned to themselves
    IF p_role IN ('COACH', 'ADMIN') AND NOT public.is_admin() THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Koç veya Yönetici davet kodunu yalnızca Sistem Yöneticisi oluşturabilir.';
    END IF;

    IF NOT (public.is_admin() OR (public.is_coach() AND p_role = 'STUDENT')) THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Sadece Admin ve Koç davet kodu oluşturabilir.';
    END IF;

    -- If coach is creating, force coach assignment to themselves
    IF public.is_coach() AND NOT public.is_admin() THEN
        p_assigned_coach_id := v_creator_id;
    END IF;

    -- Generate random 8-character uppercase alphanumeric code (e.g. MHFZ-8921)
    v_raw_code := 'MHFZ-' || upper(encode(gen_random_bytes(4), 'hex'));
    v_code_hash := encode(digest(v_raw_code, 'sha256'), 'hex');
    v_expires_at := NOW() + (p_valid_days || ' days')::INTERVAL;

    INSERT INTO public.invitations (
        code_hash, role, assigned_coach_id, expires_at, created_by, metadata
    ) VALUES (
        v_code_hash, p_role, p_assigned_coach_id, v_expires_at, v_creator_id, p_metadata
    ) RETURNING id INTO v_new_id;

    RETURN QUERY SELECT v_new_id, v_raw_code, v_expires_at;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, extensions;

REVOKE EXECUTE ON FUNCTION public.create_invitation(user_role, UUID, INTEGER, JSONB) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.create_invitation(user_role, UUID, INTEGER, JSONB) TO authenticated;

-- ==============================================================================
-- 4. PUBLIC ATOMIC INVITATION VALIDATION FUNCTION
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.validate_invitation(p_plain_code TEXT)
RETURNS TABLE(
    is_valid BOOLEAN, 
    role user_role, 
    coach_name VARCHAR(255), 
    message TEXT
) AS $$
DECLARE
    v_hash TEXT;
    v_inv RECORD;
BEGIN
    IF p_plain_code IS NULL OR length(trim(p_plain_code)) = 0 THEN
        RETURN QUERY SELECT false, NULL::user_role, NULL::VARCHAR(255), 'Davet kodu boş olamaz.'::TEXT;
        RETURN;
    END IF;

    v_hash := encode(digest(trim(p_plain_code), 'sha256'), 'hex');

    SELECT i.*, p.name AS coach_display_name INTO v_inv
    FROM public.invitations i
    LEFT JOIN public.profiles p ON p.id = i.assigned_coach_id
    WHERE i.code_hash = v_hash;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, NULL::user_role, NULL::VARCHAR(255), 'Geçersiz davet kodu.'::TEXT;
        RETURN;
    END IF;

    IF v_inv.used_at IS NOT NULL THEN
        RETURN QUERY SELECT false, NULL::user_role, NULL::VARCHAR(255), 'Bu davet kodu daha önce kullanılmıştır.'::TEXT;
        RETURN;
    END IF;

    IF v_inv.revoked_at IS NOT NULL THEN
        RETURN QUERY SELECT false, NULL::user_role, NULL::VARCHAR(255), 'Bu davet kodu iptal edilmiştir.'::TEXT;
        RETURN;
    END IF;

    IF v_inv.expires_at < NOW() THEN
        RETURN QUERY SELECT false, NULL::user_role, NULL::VARCHAR(255), 'Bu davet kodunun süresi dolmuştur.'::TEXT;
        RETURN;
    END IF;

    RETURN QUERY SELECT true, v_inv.role, v_inv.coach_display_name, 'Davet kodu geçerli.'::TEXT;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER SET search_path = public, extensions;

-- Allow anon and authenticated to validate codes prior to signup
GRANT EXECUTE ON FUNCTION public.validate_invitation(TEXT) TO anon, authenticated;

-- ==============================================================================
-- 5. ATOMIC INVITATION CONSUMPTION AND PROFILE LINKING (Internal Trigger Use Only)
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.consume_invitation_atomically(
    p_plain_code TEXT,
    p_user_id UUID
)
RETURNS TABLE(success BOOLEAN, assigned_role user_role, assigned_coach UUID, is_founder BOOLEAN, error_message TEXT) AS $$
DECLARE
    v_hash TEXT;
    v_inv RECORD;
BEGIN
    -- Block any authenticated client trying to invoke this directly via RPC
    IF auth.role() = 'authenticated' AND current_user NOT IN ('postgres', 'supabase_admin') THEN
        RAISE EXCEPTION 'Yetkisiz erişim: Davet kodları doğrudan istemci RPC çağrısıyla tüketilemez.';
    END IF;

    IF p_plain_code IS NULL OR length(trim(p_plain_code)) = 0 THEN
        RETURN QUERY SELECT false, NULL::user_role, NULL::UUID, false, 'Davet kodu zorunludur.'::TEXT;
        RETURN;
    END IF;

    v_hash := encode(digest(trim(p_plain_code), 'sha256'), 'hex');

    -- Concurrency safe lock: FOR UPDATE strictly serializes concurrent consumption
    SELECT id, role, assigned_coach_id, metadata INTO v_inv
    FROM public.invitations
    WHERE code_hash = v_hash 
      AND used_at IS NULL 
      AND revoked_at IS NULL 
      AND expires_at > NOW()
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, NULL::user_role, NULL::UUID, false, 'Geçersiz, kullanılmış veya süresi dolmuş davet kodu.'::TEXT;
        RETURN;
    END IF;

    -- If this is a founder bootstrap code, verify that NO admin currently exists
    IF (v_inv.metadata->>'type') = 'FOUNDER_BOOTSTRAP' THEN
        IF EXISTS (SELECT 1 FROM public.profiles WHERE role = 'ADMIN'::public.user_role) THEN
            RETURN QUERY SELECT false, NULL::user_role, NULL::UUID, false, 'Bootstrap kilitli: Sistemde zaten bir Admin hesabı bulunmaktadır.'::TEXT;
            RETURN;
        END IF;
    END IF;

    -- Mark invitation as used in the same transaction
    UPDATE public.invitations
    SET used_at = NOW(),
        used_by = p_user_id
    WHERE id = v_inv.id;

    RETURN QUERY SELECT true, v_inv.role, v_inv.assigned_coach_id, ((v_inv.metadata->>'type') = 'FOUNDER_BOOTSTRAP'), NULL::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, extensions;

-- CRITICAL HARDENING: Revoke execution from PUBLIC, anon, and authenticated
REVOKE ALL ON FUNCTION public.consume_invitation_atomically(TEXT, UUID) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.consume_invitation_atomically(TEXT, UUID) TO service_role;

-- ==============================================================================
-- 6. AUTH.USERS AUTO-PROVISIONING TRIGGER (STRICT INVITATION-ONLY POLICY)
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_auth_user()
RETURNS TRIGGER AS $$
DECLARE
    v_invite_code TEXT;
    v_user_name TEXT;
    v_consume_result RECORD;
BEGIN
    -- Extract metadata sent from client during signUp()
    v_user_name := COALESCE(NEW.raw_user_meta_data->>'name', split_part(NEW.email, '@', 1));
    v_invite_code := NEW.raw_user_meta_data->>'invitation_code';

    -- CRITICAL DEFENSE: Reject any registration without an invitation code!
    IF v_invite_code IS NULL OR length(trim(v_invite_code)) = 0 THEN
        RAISE EXCEPTION 'Kayıt Başarısız: Mahfaza.co platformuna davet kodu olmadan kayıt olunamaz. Lütfen koçunuzdan veya kurumunuzdan davet kodu alınız.';
    END IF;

    -- Atomically lock and consume the code
    SELECT * INTO v_consume_result 
    FROM public.consume_invitation_atomically(v_invite_code, NEW.id);

    -- If invitation consumption failed (expired, invalid, already used), abort transaction completely!
    IF NOT v_consume_result.success THEN
        RAISE EXCEPTION 'Kayıt Başarısız: %', v_consume_result.error_message;
    END IF;

    -- Provision profile with ONLY the authorized role and coach association from the invitation
    INSERT INTO public.profiles (
        id, 
        email, 
        name, 
        role, 
        assigned_coach_id, 
        created_at, 
        updated_at
    ) VALUES (
        NEW.id, 
        NEW.email, 
        v_user_name, 
        v_consume_result.assigned_role, 
        v_consume_result.assigned_coach, 
        NOW(), 
        NOW()
    );

    -- If founder bootstrap code was consumed, permanently close the bootstrap lock
    IF v_consume_result.is_founder IS TRUE THEN
        UPDATE public.system_settings 
        SET value = 'true'::jsonb, updated_at = NOW() 
        WHERE key = 'founder_bootstrap_completed';

        -- Revoke all remaining founder bootstrap codes immediately
        UPDATE public.invitations
        SET revoked_at = NOW()
        WHERE metadata->>'type' = 'FOUNDER_BOOTSTRAP' AND used_at IS NULL;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, auth, extensions;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
AFTER INSERT ON auth.users
FOR EACH ROW EXECUTE FUNCTION public.handle_new_auth_user();
