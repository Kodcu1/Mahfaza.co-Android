-- ==============================================================================
-- MAHFAZA.CO V34.2 — ROW LEVEL SECURITY (RLS) POLICIES & INTEGRITY GUARDS
-- Zero-Trust Security Policies with Role-Based & Coach-Student Isolation
-- File: supabase/migrations/0002_rls_policies.sql
-- ==============================================================================

-- 1. HELPER SECURITY DEFINER FUNCTIONS (Guarded search_path)
CREATE OR REPLACE FUNCTION public.get_auth_user_role()
RETURNS public.user_role AS $$
    SELECT role FROM public.profiles WHERE id = auth.uid();
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, auth;

CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.profiles 
        WHERE id = auth.uid() AND role = 'ADMIN'::public.user_role
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, auth;

CREATE OR REPLACE FUNCTION public.is_coach()
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.profiles 
        WHERE id = auth.uid() AND role = 'COACH'::public.user_role
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, auth;

CREATE OR REPLACE FUNCTION public.is_coach_of(p_student_id UUID)
RETURNS BOOLEAN AS $$
    SELECT EXISTS (
        SELECT 1 FROM public.profiles 
        WHERE id = p_student_id AND assigned_coach_id = auth.uid()
    );
$$ LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, auth;

-- Restrict function execution to authenticated users only
REVOKE EXECUTE ON FUNCTION public.get_auth_user_role() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.get_auth_user_role() TO authenticated;

REVOKE EXECUTE ON FUNCTION public.is_admin() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_admin() TO authenticated;

REVOKE EXECUTE ON FUNCTION public.is_coach() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_coach() TO authenticated;

REVOKE EXECUTE ON FUNCTION public.is_coach_of(UUID) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.is_coach_of(UUID) TO authenticated;

-- ==============================================================================
-- 2. ENABLE ROW LEVEL SECURITY ON ALL TABLES
-- ==============================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exam_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.subject_results ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.xp_ledger ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pomodoro_sessions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.rewards ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reward_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.parent_meetings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.package_configs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.financial_transactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.reminder_settings ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.file_metadata ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.sync_operations ENABLE ROW LEVEL SECURITY;

-- ==============================================================================
-- 3. PROFILES POLICIES & IMMUTABLE FIELD GUARD TRIGGER
-- ==============================================================================
-- Trigger to prevent client-side privilege escalation (e.g. self-promoting to ADMIN)
CREATE OR REPLACE FUNCTION public.guard_profile_immutable_fields()
RETURNS TRIGGER AS $$
BEGIN
    -- If updater is Admin, allow all modifications
    IF public.is_admin() THEN
        RETURN NEW;
    END IF;

    -- Non-admin users cannot alter role, assigned coach, xp, package, streak or coach code
    IF NEW.role IS DISTINCT FROM OLD.role THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Kullanıcı rolü istemciden değiştirilemez.';
    END IF;

    IF NEW.email IS DISTINCT FROM OLD.email THEN
        RAISE EXCEPTION 'Yetkisiz işlem: E-posta adresi doğrudan profil güncellemesiyle değiştirilemez.';
    END IF;

    IF NEW.assigned_coach_id IS DISTINCT FROM OLD.assigned_coach_id THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Koç ataması istemciden değiştirilemez.';
    END IF;

    IF NEW.current_xp IS DISTINCT FROM OLD.current_xp OR NEW.lifetime_xp IS DISTINCT FROM OLD.lifetime_xp THEN
        RAISE EXCEPTION 'Yetkisiz işlem: XP değerleri doğrudan profil güncellemesiyle değiştirilemez.';
    END IF;

    IF NEW.streak IS DISTINCT FROM OLD.streak THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Seri sayısı doğrudan profil güncellemesiyle değiştirilemez.';
    END IF;

    IF NEW.package_id IS DISTINCT FROM OLD.package_id THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Paket tanımları doğrudan profil güncellemesiyle değiştirilemez.';
    END IF;

    IF NEW.subscription_plan IS DISTINCT FROM OLD.subscription_plan THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Abonelik planı doğrudan profil güncellemesiyle değiştirilemez.';
    END IF;

    IF NEW.student_capacity IS DISTINCT FROM OLD.student_capacity THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Öğrenci kontenjanı doğrudan profil güncellemesiyle değiştirilemez.';
    END IF;

    IF NEW.coach_code IS DISTINCT FROM OLD.coach_code THEN
        RAISE EXCEPTION 'Yetkisiz işlem: Koç kodu doğrudan profil güncellemesiyle değiştirilemez.';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = public;

DROP TRIGGER IF EXISTS trg_guard_profile_immutable_fields ON public.profiles;
CREATE TRIGGER trg_guard_profile_immutable_fields
BEFORE UPDATE ON public.profiles
FOR EACH ROW EXECUTE FUNCTION public.guard_profile_immutable_fields();

-- Users can view their own profile, coaches can view their assigned students, students can view their assigned coach, admins can view all
CREATE POLICY "profiles_select_policy" ON public.profiles
FOR SELECT USING (
    auth.uid() = id 
    OR public.is_admin() 
    OR (public.is_coach() AND assigned_coach_id = auth.uid())
    OR (id = (SELECT p.assigned_coach_id FROM public.profiles p WHERE p.id = auth.uid()))
);

CREATE POLICY "profiles_update_policy" ON public.profiles
FOR UPDATE USING (
    auth.uid() = id OR public.is_admin()
) WITH CHECK (
    auth.uid() = id OR public.is_admin()
);

-- ==============================================================================
-- 4. TASKS POLICIES (Enforce is_approved = false on student insertion)
-- ==============================================================================
CREATE POLICY "tasks_select_policy" ON public.tasks
FOR SELECT USING (
    auth.uid() = user_id 
    OR public.is_coach_of(user_id) 
    OR public.is_admin()
);

-- Student can only insert unapproved, unarchived tasks; Coach can assign to students
CREATE POLICY "tasks_insert_policy" ON public.tasks
FOR INSERT WITH CHECK (
    (
        auth.uid() = user_id 
        AND is_approved = false 
        AND is_rejected = false 
        AND is_archived = false
        AND xp_reward <= 40
    ) 
    OR public.is_coach_of(user_id) 
    OR public.is_admin()
);

-- Student can only update unapproved tasks; Coach can approve/reject; Admin full update
CREATE POLICY "tasks_update_policy" ON public.tasks
FOR UPDATE USING (
    auth.uid() = user_id 
    OR public.is_coach_of(user_id) 
    OR public.is_admin()
) WITH CHECK (
    (auth.uid() = user_id AND is_approved = false AND is_rejected = false) 
    OR public.is_coach_of(user_id) 
    OR public.is_admin()
);

CREATE POLICY "tasks_delete_policy" ON public.tasks
FOR DELETE USING (
    (auth.uid() = user_id AND is_approved = false) 
    OR public.is_coach_of(user_id) 
    OR public.is_admin()
);

-- ==============================================================================
-- 5. EXAM RESULTS & SUBJECT RESULTS POLICIES
-- ==============================================================================
CREATE POLICY "exam_results_select" ON public.exam_results
FOR SELECT USING (
    auth.uid() = user_id OR public.is_coach_of(user_id) OR public.is_admin()
);

CREATE POLICY "exam_results_insert" ON public.exam_results
FOR INSERT WITH CHECK (
    auth.uid() = user_id OR public.is_coach_of(user_id) OR public.is_admin()
);

CREATE POLICY "exam_results_update" ON public.exam_results
FOR UPDATE USING (
    auth.uid() = user_id OR public.is_coach_of(user_id) OR public.is_admin()
);

CREATE POLICY "exam_results_delete" ON public.exam_results
FOR DELETE USING (
    auth.uid() = user_id OR public.is_admin()
);

CREATE POLICY "subject_results_select" ON public.subject_results
FOR SELECT USING (
    EXISTS (
        SELECT 1 FROM public.exam_results e 
        WHERE e.id = exam_id AND (e.user_id = auth.uid() OR public.is_coach_of(e.user_id) OR public.is_admin())
    )
);

CREATE POLICY "subject_results_insert" ON public.subject_results
FOR INSERT WITH CHECK (
    EXISTS (
        SELECT 1 FROM public.exam_results e 
        WHERE e.id = exam_id AND (e.user_id = auth.uid() OR public.is_coach_of(e.user_id) OR public.is_admin())
    )
);

-- ==============================================================================
-- 6. XP LEDGER (Immutable Ledger — SELECT own/coach/admin, INSERT only via RPC)
-- ==============================================================================
CREATE POLICY "xp_ledger_select" ON public.xp_ledger
FOR SELECT USING (
    auth.uid() = user_id OR public.is_coach_of(user_id) OR public.is_admin()
);

-- INSERT restricted to Admin only (Regular XP is inserted by SECURITY DEFINER RPC)
CREATE POLICY "xp_ledger_insert_admin_only" ON public.xp_ledger
FOR INSERT WITH CHECK (
    public.is_admin()
);

-- ==============================================================================
-- 7. POMODORO SESSIONS POLICIES
-- ==============================================================================
CREATE POLICY "pomodoro_select" ON public.pomodoro_sessions
FOR SELECT USING (
    auth.uid() = student_id OR public.is_coach_of(student_id) OR public.is_admin()
);

CREATE POLICY "pomodoro_insert" ON public.pomodoro_sessions
FOR INSERT WITH CHECK (
    auth.uid() = student_id OR public.is_admin()
);

-- ==============================================================================
-- 8. MESSAGES POLICIES (Strict Sender & Receiver Isolation)
-- ==============================================================================
CREATE POLICY "messages_select" ON public.messages
FOR SELECT USING (
    auth.uid() = sender_id OR auth.uid() = receiver_id OR public.is_admin()
);

CREATE POLICY "messages_insert" ON public.messages
FOR INSERT WITH CHECK (
    auth.uid() = sender_id
);

CREATE POLICY "messages_update_read" ON public.messages
FOR UPDATE USING (
    auth.uid() = receiver_id
) WITH CHECK (
    auth.uid() = receiver_id
);

-- ==============================================================================
-- 9. NOTIFICATIONS POLICIES
-- ==============================================================================
CREATE POLICY "notifications_select" ON public.notifications
FOR SELECT USING (
    auth.uid() = recipient_id OR public.is_admin()
);

CREATE POLICY "notifications_update" ON public.notifications
FOR UPDATE USING (
    auth.uid() = recipient_id
) WITH CHECK (
    auth.uid() = recipient_id
);

-- ==============================================================================
-- 10. REWARDS & REWARD REQUESTS POLICIES
-- ==============================================================================
CREATE POLICY "rewards_select" ON public.rewards
FOR SELECT USING (true);

CREATE POLICY "rewards_admin_modify" ON public.rewards
FOR ALL USING (public.is_admin());

CREATE POLICY "reward_requests_select" ON public.reward_requests
FOR SELECT USING (
    auth.uid() = student_id OR public.is_coach_of(student_id) OR public.is_admin()
);

CREATE POLICY "reward_requests_insert" ON public.reward_requests
FOR INSERT WITH CHECK (
    auth.uid() = student_id
);

CREATE POLICY "reward_requests_update" ON public.reward_requests
FOR UPDATE USING (
    public.is_coach_of(student_id) OR public.is_admin()
);

-- ==============================================================================
-- 11. PARENT MEETINGS POLICIES (Student strictly BLOCKED from seeing parent notes)
-- ==============================================================================
CREATE POLICY "parent_meetings_access" ON public.parent_meetings
FOR ALL USING (
    auth.uid() = coach_id OR public.is_coach_of(student_id) OR public.is_admin()
) WITH CHECK (
    (auth.uid() = coach_id AND public.is_coach_of(student_id)) OR public.is_admin()
);

-- ==============================================================================
-- 12. FINANCIAL TRANSACTIONS & PACKAGE CONFIGS POLICIES (Admin Only)
-- ==============================================================================
CREATE POLICY "package_configs_select" ON public.package_configs
FOR SELECT USING (true);

CREATE POLICY "package_configs_admin" ON public.package_configs
FOR ALL USING (public.is_admin());

CREATE POLICY "financial_transactions_admin_only" ON public.financial_transactions
FOR ALL USING (public.is_admin());

-- ==============================================================================
-- 13. REMINDER SETTINGS POLICIES
-- ==============================================================================
CREATE POLICY "reminder_settings_user" ON public.reminder_settings
FOR ALL USING (auth.uid() = user_id);

-- ==============================================================================
-- 14. FILE METADATA POLICIES
-- ==============================================================================
CREATE POLICY "file_metadata_select" ON public.file_metadata
FOR SELECT USING (
    auth.uid() = owner_id 
    OR public.is_coach_of(owner_id) 
    OR access_level = 'PUBLIC' 
    OR public.is_admin()
);

CREATE POLICY "file_metadata_insert" ON public.file_metadata
FOR INSERT WITH CHECK (
    auth.uid() = owner_id OR public.is_admin()
);

-- ==============================================================================
-- 15. SYNC OPERATIONS POLICIES
-- ==============================================================================
CREATE POLICY "sync_operations_user" ON public.sync_operations
FOR ALL USING (
    auth.uid() = user_id OR public.is_admin()
);
