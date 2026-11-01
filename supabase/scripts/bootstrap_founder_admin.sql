-- ==============================================================================
-- MAHFAZA.CO V34.4 — FOUNDER ADMIN SECURE BOOTSTRAP SCRIPT
-- Single-Use, Auditable, Zero-Client-Privilege Founder Account Initialization
-- File: supabase/scripts/bootstrap_founder_admin.sql
-- ==============================================================================
-- KULLANIM TALİMATI:
-- Bu script YALNIZCA Supabase Dashboard > SQL Editor üzerinden ve sistem kurucusu
-- tarafından TEK SEFERLİK çalıştırılmalıdır.
-- Mobil istemciden, anonim kullanıcılardan veya API üzerinden çağrılamaz.
--
-- Güvenlik Kuralları:
-- 1. PostgreSQL transaction düzeyinde kilit (pg_advisory_xact_lock) kullanır; eşzamanlı çift çalıştırılamaz.
-- 2. system_settings tablosundaki 'founder_bootstrap_completed' kilidini kontrol eder.
-- 3. Sistemde kayıtlı en az 1 ADMIN varsa script anında işlemi durdurur (Abort).
-- 4. Normal öğrenci/koç kayıt akışını bozmaz; davetsiz kayıt açığını kullanmaz.
-- 5. Kriptografik olarak güvenli tek kullanımlık FOUNDER davet kodu üretir (24 saat geçerli).
-- 6. Kurucu bu kodu kullanarak kendi gerçek şifresi ve e-postasıyla kayıt olur.
-- 7. Kod tek kullanımlıktır; kayıt tamamlandığı an on_auth_user_created trigger'ı
--    bootstrap kilidini kalıcı olarak kapatır ve tüm founder kodlarını iptal eder.
-- ==============================================================================

DO $$
DECLARE
    v_existing_admin_count INTEGER;
    v_is_bootstrapped BOOLEAN;
    v_raw_code TEXT;
    v_code_hash TEXT;
    v_expires_at TIMESTAMPTZ;
    v_invitation_id UUID;
BEGIN
    -- 1. EŞZAMANLILIK KİLİDİ: İki operatörün aynı anda çalıştırmasını engelle
    PERFORM pg_advisory_xact_lock(84729104);

    -- 2. GÜVENLİK KONTROLÜ: Bootstrap kilidi daha önce kapatılmış mı?
    SELECT (value->>0)::BOOLEAN INTO v_is_bootstrapped
    FROM public.system_settings 
    WHERE key = 'founder_bootstrap_completed';

    IF v_is_bootstrapped IS TRUE THEN
        RAISE EXCEPTION 'BOOTSTRAP KİLİTLİ: Kurucu Admin oluşturma süreci daha önce tamamlanmış ve kalıcı olarak kapatılmıştır.';
    END IF;

    -- 3. GÜVENLİK KONTROLÜ: Mevcut Admin Var mı?
    SELECT COUNT(*) INTO v_existing_admin_count 
    FROM public.profiles 
    WHERE role = 'ADMIN'::public.user_role;

    IF v_existing_admin_count > 0 THEN
        UPDATE public.system_settings 
        SET value = 'true'::jsonb, updated_at = NOW() 
        WHERE key = 'founder_bootstrap_completed';
        
        RAISE EXCEPTION 'BOOTSTRAP REDDEDİLDİ: Sistemde zaten % adet aktif ADMIN hesabı kayıtlıdır. Bootstrap yolu kalıcı olarak kilitlendi.', v_existing_admin_count;
    END IF;

    -- 4. Eski, kullanılmamış founder kodlarını temizle (varsa)
    UPDATE public.invitations
    SET revoked_at = NOW()
    WHERE metadata->>'type' = 'FOUNDER_BOOTSTRAP' AND used_at IS NULL;

    -- 5. Tek Kullanımlık Kriptografik Founder Kodu Üret (24 Saat Geçerli)
    -- Format: FOUNDER-XXXX-XXXX
    v_raw_code := 'FOUNDER-' || upper(encode(gen_random_bytes(3), 'hex')) || '-' || upper(encode(gen_random_bytes(3), 'hex'));
    v_code_hash := encode(digest(v_raw_code, 'sha256'), 'hex');
    v_expires_at := NOW() + INTERVAL '24 hours';

    -- 6. invitations tablosuna güvenli şekilde kaydet (created_by NULL = Sistem Bootstrap)
    INSERT INTO public.invitations (
        code_hash,
        role,
        assigned_coach_id,
        expires_at,
        created_by,
        metadata
    ) VALUES (
        v_code_hash,
        'ADMIN'::public.user_role,
        NULL,
        v_expires_at,
        NULL,
        jsonb_build_object(
            'type', 'FOUNDER_BOOTSTRAP',
            'created_via', 'SUPABASE_DASHBOARD_SQL_EDITOR',
            'created_at_utc', NOW()
        )
    ) RETURNING id INTO v_invitation_id;

    -- 7. Operatör Konsoluna Bilgilendirme Mesajı Bas
    RAISE NOTICE '==============================================================================';
    RAISE NOTICE 'MAHFAZA.CO KURUCU ADMIN BOOTSTRAP KODU BAŞARIYLA OLUŞTURULDU!';
    RAISE NOTICE '------------------------------------------------------------------------------';
    RAISE NOTICE 'KULLANIM ADIMLARI:';
    RAISE NOTICE '1. Mahfaza mobil uygulamasını veya kayıt ekranını açın.';
    RAISE NOTICE '2. Kurucu Admin olarak kullanacağınız gerçek e-posta ve şifrenizi girin.';
    RAISE NOTICE '3. Davet kodu alanına aşağıdaki TEK KULLANIMLIK kodu yapıştırın:';
    RAISE NOTICE '';
    RAISE NOTICE '   DAVET KODUNUZ: %', v_raw_code;
    RAISE NOTICE '';
    RAISE NOTICE '4. Kayıt tamamlandığı anda hesabınız otomatik olarak ADMIN rolüne atanacaktır.';
    RAISE NOTICE '5. Bu kod 24 saat sonra geçersiz olacaktır (%):', v_expires_at;
    RAISE NOTICE '==============================================================================';

END $$;
