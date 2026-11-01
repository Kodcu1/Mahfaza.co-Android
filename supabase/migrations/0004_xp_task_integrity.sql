-- ==============================================================================
-- MAHFAZA.CO V34.4 — XP INTEGRITY, TASK APPROVAL & REWARD REDEMPTION
-- Atomic Transactions, Daily Caps, Anti-Double-Spend & Task State Protection
-- File: supabase/migrations/0004_xp_task_integrity.sql
-- ==============================================================================

-- 1. CONSTANTS & SYSTEM CONFIG
CREATE TABLE IF NOT EXISTS public.system_settings (
    key VARCHAR(64) PRIMARY KEY,
    value JSONB NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- RLS ON SYSTEM SETTINGS
ALTER TABLE public.system_settings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "system_settings_select_all" ON public.system_settings
FOR SELECT USING (true);

CREATE POLICY "system_settings_admin_all" ON public.system_settings
FOR ALL USING (public.is_admin());

REVOKE ALL ON public.system_settings FROM PUBLIC;
GRANT SELECT ON public.system_settings TO authenticated, anon;
GRANT ALL ON public.system_settings TO service_role;

INSERT INTO public.system_settings (key, value, description)
VALUES 
    ('xp_daily_cap', '500'::jsonb, 'Maksimum günlük kazanılabilecek XP miktarı'),
    ('max_question_xp', '40'::jsonb, 'Tek çalışma başına verilebilecek azami soru XP'),
    ('max_task_xp', '100'::jsonb, 'Tek bir görev için verilebilecek azami XP tavanı'),
    ('founder_bootstrap_completed', 'false'::jsonb, 'Kurucu Admin hesabının oluşturulup tamamlandığını belirten sistem kilidi')
ON CONFLICT (key) DO NOTHING;

-- ==============================================================================
-- 2. ATOMIC TASK APPROVAL AND XP AWARDING FUNCTION
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.approve_task_and_award_xp(
    p_task_id UUID,
    p_override_xp INTEGER DEFAULT NULL
)
RETURNS TABLE(
    success BOOLEAN, 
    awarded_xp INTEGER, 
    student_total_xp INTEGER, 
    error_message TEXT
) AS $$
DECLARE
    v_task RECORD;
    v_student RECORD;
    v_coach_id UUID;
    v_xp_to_award INTEGER;
    v_daily_cap INTEGER := 500;
    v_max_task_xp INTEGER := 100;
    v_today_start TIMESTAMPTZ;
    v_today_xp_sum INTEGER;
    v_source_ref TEXT;
BEGIN
    v_coach_id := auth.uid();
    
    -- Correct Istanbul Timezone (Europe/Istanbul UTC+3) daily start calculation
    v_today_start := date_trunc('day', NOW() AT TIME ZONE 'Europe/Istanbul') AT TIME ZONE 'Europe/Istanbul';

    -- Fetch system settings
    SELECT (value->>0)::INTEGER INTO v_daily_cap 
    FROM public.system_settings WHERE key = 'xp_daily_cap';
    IF v_daily_cap IS NULL THEN v_daily_cap := 500; END IF;

    SELECT (value->>0)::INTEGER INTO v_max_task_xp 
    FROM public.system_settings WHERE key = 'max_task_xp';
    IF v_max_task_xp IS NULL THEN v_max_task_xp := 100; END IF;

    -- Concurrency safe lock on task
    SELECT * INTO v_task 
    FROM public.tasks 
    WHERE id = p_task_id 
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, 0, 0, 'Görev bulunamadı.'::TEXT;
        RETURN;
    END IF;

    -- Authorization check: caller must be assigned coach or admin
    IF NOT (public.is_admin() OR public.is_coach_of(v_task.user_id)) THEN
        RETURN QUERY SELECT false, 0, 0, 'Bu görevi onaylama yetkiniz bulunmuyor.'::TEXT;
        RETURN;
    END IF;

    -- Self-approval guard: Student cannot approve their own task
    IF v_task.user_id = v_coach_id AND NOT public.is_admin() THEN
        RETURN QUERY SELECT false, 0, 0, 'Öğrenci kendi görevini onaylayamaz.'::TEXT;
        RETURN;
    END IF;

    -- State validation: Double approval guard
    IF v_task.is_approved THEN
        RETURN QUERY SELECT false, 0, 0, 'Bu görev zaten onaylanmış.'::TEXT;
        RETURN;
    END IF;

    -- Validate override XP if supplied
    IF p_override_xp IS NOT NULL THEN
        IF p_override_xp <= 0 OR p_override_xp > v_max_task_xp THEN
            RETURN QUERY SELECT false, 0, 0, 'Geçersiz XP değeri: Belirlenen görev tavanı aşıldı.'::TEXT;
            RETURN;
        END IF;
    END IF;

    -- Concurrency safe lock on student profile to serialize XP computations
    -- and prevent race condition on daily cap calculation across parallel calls
    PERFORM 1 FROM public.profiles 
    WHERE id = v_task.user_id 
    FOR UPDATE;

    -- Calculate XP
    v_xp_to_award := COALESCE(p_override_xp, v_task.xp_reward);
    IF v_xp_to_award < 0 THEN v_xp_to_award := 0; END IF;
    IF v_xp_to_award > v_max_task_xp THEN v_xp_to_award := v_max_task_xp; END IF;

    -- Check daily cap for student in Europe/Istanbul timezone
    SELECT COALESCE(SUM(amount), 0) INTO v_today_xp_sum
    FROM public.xp_ledger
    WHERE user_id = v_task.user_id 
      AND type = 'EARNED' 
      AND created_at >= v_today_start;

    IF (v_today_xp_sum + v_xp_to_award) > v_daily_cap THEN
        v_xp_to_award := GREATEST(0, v_daily_cap - v_today_xp_sum);
    END IF;

    v_source_ref := 'TASK_APPROVAL:' || p_task_id;

    -- Mark Task as approved
    UPDATE public.tasks
    SET is_approved = true,
        is_completed = true,
        is_rejected = false,
        updated_at = NOW()
    WHERE id = p_task_id;

    -- Insert into XP Ledger (protected by UNIQUE index on user_id + source_reference)
    IF v_xp_to_award > 0 THEN
        INSERT INTO public.xp_ledger (
            user_id, amount, type, reason, source_reference, created_at
        ) VALUES (
            v_task.user_id, 
            v_xp_to_award, 
            'EARNED', 
            'Çalışma Onayı: ' || v_task.title, 
            v_source_ref, 
            NOW()
        );

        -- Update Student Profile XP totals
        UPDATE public.profiles
        SET current_xp = current_xp + v_xp_to_award,
            lifetime_xp = lifetime_xp + v_xp_to_award
        WHERE id = v_task.user_id
        RETURNING current_xp INTO v_student.current_xp;
    ELSE
        SELECT current_xp INTO v_student.current_xp 
        FROM public.profiles WHERE id = v_task.user_id;
    END IF;

    -- Auto-insert in-app notification for the student
    INSERT INTO public.notifications (
        recipient_id, type, title, message, related_entity_id, idempotency_key
    ) VALUES (
        v_task.user_id,
        'STUDY_APPROVED',
        'Çalışman Onaylandı! ✨',
        v_task.title || ' çalışman koçun tarafından onaylandı (+' || v_xp_to_award || ' XP).',
        p_task_id::TEXT,
        'NOTIF_APPROVAL:' || p_task_id
    ) ON CONFLICT (recipient_id, idempotency_key) DO NOTHING;

    RETURN QUERY SELECT true, v_xp_to_award, v_student.current_xp, NULL::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- ==============================================================================
-- 3. ATOMIC REWARD REQUEST & XP SPEND
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.request_reward_redemption(
    p_reward_id UUID
)
RETURNS TABLE(
    success BOOLEAN, 
    request_id UUID, 
    remaining_xp INTEGER, 
    error_message TEXT
) AS $$
DECLARE
    v_user_id UUID;
    v_user RECORD;
    v_reward RECORD;
    v_new_req_id UUID;
BEGIN
    v_user_id := auth.uid();

    -- Strict Hierarchical Lock Order: rewards -> profiles -> reward_requests
    -- 1. Lock reward
    SELECT id, name, xp_cost, stock INTO v_reward 
    FROM public.rewards 
    WHERE id = p_reward_id 
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, NULL::UUID, 0, 'Ödül bulunamadı.'::TEXT;
        RETURN;
    END IF;

    -- 2. Lock student profile
    SELECT id, current_xp, name INTO v_user 
    FROM public.profiles 
    WHERE id = v_user_id 
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, NULL::UUID, 0, 'Kullanıcı profili bulunamadı.'::TEXT;
        RETURN;
    END IF;

    -- 3. Prevent duplicate active pending requests for the same reward
    IF EXISTS (
        SELECT 1 FROM public.reward_requests 
        WHERE student_id = v_user_id AND reward_id = p_reward_id AND status = 'PENDING'
    ) THEN
        RETURN QUERY SELECT false, NULL::UUID, v_user.current_xp, 'Bu ödül için zaten beklemede olan bir talebiniz mevcuttur.'::TEXT;
        RETURN;
    END IF;

    IF v_reward.stock <= 0 THEN
        RETURN QUERY SELECT false, NULL::UUID, v_user.current_xp, 'Bu ödülün stoğu tükenmiştir.'::TEXT;
        RETURN;
    END IF;

    IF v_user.current_xp < v_reward.xp_cost THEN
        RETURN QUERY SELECT false, NULL::UUID, v_user.current_xp, 'Yetersiz XP bakiyesi.'::TEXT;
        RETURN;
    END IF;

    INSERT INTO public.reward_requests (
        student_id, reward_id, xp_cost, status, created_at, updated_at
    ) VALUES (
        v_user_id, p_reward_id, v_reward.xp_cost, 'PENDING', NOW(), NOW()
    ) RETURNING id INTO v_new_req_id;

    RETURN QUERY SELECT true, v_new_req_id, v_user.current_xp, NULL::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- ==============================================================================
-- 4. ATOMIC REWARD APPROVAL (Coach deducts XP and decrements stock)
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.process_reward_request(
    p_request_id UUID,
    p_status reward_request_status
)
RETURNS TABLE(success BOOLEAN, error_message TEXT) AS $$
DECLARE
    v_req RECORD;
    v_coach_id UUID;
    v_reward RECORD;
    v_student RECORD;
BEGIN
    v_coach_id := auth.uid();

    -- Strict Hierarchical Lock Order: reward_requests -> rewards -> profiles
    -- 1. Lock reward request
    SELECT r.*, rew.name as rew_name INTO v_req 
    FROM public.reward_requests r
    JOIN public.rewards rew ON rew.id = r.reward_id
    WHERE r.id = p_request_id 
    FOR UPDATE;

    IF NOT FOUND THEN
        RETURN QUERY SELECT false, 'Talep bulunamadı.'::TEXT;
        RETURN;
    END IF;

    IF v_req.status != 'PENDING' THEN
        RETURN QUERY SELECT false, 'Bu talep zaten sonuçlandırılmış.'::TEXT;
        RETURN;
    END IF;

    IF NOT (public.is_admin() OR public.is_coach_of(v_req.student_id)) THEN
        RETURN QUERY SELECT false, 'Bu talebi işleme yetkiniz yok.'::TEXT;
        RETURN;
    END IF;

    -- 2. Lock reward
    SELECT id, name, stock INTO v_reward 
    FROM public.rewards 
    WHERE id = v_req.reward_id 
    FOR UPDATE;

    -- 3. Lock student profile
    SELECT id, current_xp INTO v_student 
    FROM public.profiles 
    WHERE id = v_req.student_id 
    FOR UPDATE;

    IF p_status = 'APPROVED' THEN
        -- Strict stock validation
        IF v_reward.stock <= 0 THEN
            RETURN QUERY SELECT false, 'Yetersiz stok: Ödül tükendiği için talep onaylanamaz.'::TEXT;
            RETURN;
        END IF;

        -- Strict XP balance validation
        IF v_student.current_xp < v_req.xp_cost THEN
            RETURN QUERY SELECT false, 'Yetersiz XP: Öğrencinin bakiyesi yetersiz.'::TEXT;
            RETURN;
        END IF;

        -- Deduct student XP
        UPDATE public.profiles
        SET current_xp = current_xp - v_req.xp_cost
        WHERE id = v_req.student_id;

        -- Insert Spent XP record into immutable ledger
        INSERT INTO public.xp_ledger (
            user_id, amount, type, reason, source_reference, created_at
        ) VALUES (
            v_req.student_id,
            -v_req.xp_cost,
            'SPENT',
            'Ödül Alımı: ' || v_req.rew_name,
            'REWARD_REDEMPTION:' || p_request_id,
            NOW()
        );

        -- Decrement reward stock
        UPDATE public.rewards
        SET stock = GREATEST(0, stock - 1)
        WHERE id = v_req.reward_id;
    END IF;

    -- Update request status
    UPDATE public.reward_requests
    SET status = p_status,
        processed_by = v_coach_id,
        processed_at = NOW(),
        updated_at = NOW()
    WHERE id = p_request_id;

    RETURN QUERY SELECT true, NULL::TEXT;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

-- ==============================================================================
-- 5. FUNCTION EXECUTION PERMISSION HARDENING
-- ==============================================================================
REVOKE EXECUTE ON FUNCTION public.approve_task_and_award_xp(UUID, INTEGER) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.approve_task_and_award_xp(UUID, INTEGER) TO authenticated;

REVOKE EXECUTE ON FUNCTION public.request_reward_redemption(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.request_reward_redemption(UUID) TO authenticated;

REVOKE EXECUTE ON FUNCTION public.process_reward_request(UUID, reward_request_status) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.process_reward_request(UUID, reward_request_status) TO authenticated;
