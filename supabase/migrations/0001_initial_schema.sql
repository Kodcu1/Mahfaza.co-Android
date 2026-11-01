-- ==============================================================================
-- MAHFAZA.CO V34.1 — PRODUCTION POSTGRESQL INITIAL SCHEMA
-- Based on Room Database V13 Entities & Multi-user Hybrid Backend Design
-- File: supabase/migrations/0001_initial_schema.sql
-- ==============================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. ENUM TYPES
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('STUDENT', 'COACH', 'ADMIN');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE education_level AS ENUM ('LGS_8TH', 'YKS_TYT_AYT', 'KPSS');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE xp_transaction_type AS ENUM ('EARNED', 'SPENT', 'ADJUSTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE notification_type AS ENUM (
        'TASK_ASSIGNED',
        'STUDY_SUBMITTED',
        'STUDY_APPROVED',
        'STUDY_REJECTED',
        'EXAM_SAVED',
        'EXAM_REVIEWED',
        'REWARD_REQUESTED',
        'REWARD_STATUS',
        'LEVEL_UP',
        'BADGE_UNLOCKED',
        'NEW_MESSAGE',
        'SYSTEM_ANNOUNCEMENT'
    );
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE reward_request_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE sync_operation_status AS ENUM ('PENDING', 'APPLIED', 'CONFLICT', 'REJECTED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. UPDATED_AT TRIGGER FUNCTION
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ==============================================================================
-- 4. CORE PROFILE & USER TABLE (Mapped to Room UserEntity & auth.users)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role user_role NOT NULL DEFAULT 'STUDENT',
    education_level education_level,
    package_id VARCHAR(64) NOT NULL DEFAULT 'std_free',
    current_xp INTEGER NOT NULL DEFAULT 0 CHECK (current_xp >= 0),
    lifetime_xp INTEGER NOT NULL DEFAULT 0 CHECK (lifetime_xp >= current_xp),
    streak INTEGER NOT NULL DEFAULT 0 CHECK (streak >= 0),
    student_capacity INTEGER NOT NULL DEFAULT 0 CHECK (student_capacity >= 0),
    title VARCHAR(255),
    bio TEXT,
    coaching_approach TEXT,
    expertise_tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    education JSONB NOT NULL DEFAULT '[]'::jsonb,
    experience JSONB NOT NULL DEFAULT '[]'::jsonb,
    certificates JSONB NOT NULL DEFAULT '[]'::jsonb,
    working_days JSONB NOT NULL DEFAULT '[]'::jsonb,
    working_hours VARCHAR(128),
    accepting_students BOOLEAN NOT NULL DEFAULT true,
    avatar_url TEXT,
    coach_code VARCHAR(32) UNIQUE,
    target_university VARCHAR(255),
    target_department VARCHAR(255),
    target_ranking VARCHAR(64),
    target_city VARCHAR(128),
    target_exam_date VARCHAR(64),
    daily_question_target INTEGER NOT NULL DEFAULT 200 CHECK (daily_question_target > 0),
    daily_study_target VARCHAR(64) DEFAULT '3 Saat',
    goals TEXT,
    assigned_coach_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    exam_tracks JSONB NOT NULL DEFAULT '[]'::jsonb,
    subscription_plan VARCHAR(64) NOT NULL DEFAULT 'FREE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_profiles_updated_at
BEFORE UPDATE ON public.profiles
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_profiles_role ON public.profiles(role);
CREATE INDEX IF NOT EXISTS idx_profiles_assigned_coach ON public.profiles(assigned_coach_id);
CREATE INDEX IF NOT EXISTS idx_profiles_coach_code ON public.profiles(coach_code) WHERE coach_code IS NOT NULL;

-- ==============================================================================
-- 5. TASKS TABLE (Mapped to Room TaskEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    subject VARCHAR(128) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    target_question_count INTEGER NOT NULL DEFAULT 10 CHECK (target_question_count >= 0),
    completed_question_count INTEGER NOT NULL DEFAULT 0 CHECK (completed_question_count >= 0),
    is_completed BOOLEAN NOT NULL DEFAULT false,
    priority task_priority NOT NULL DEFAULT 'MEDIUM',
    xp_reward INTEGER NOT NULL DEFAULT 10 CHECK (xp_reward >= 0),
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1: Monday, 7: Sunday
    reminder_time VARCHAR(16), -- "HH:mm"
    photo_proof_url TEXT,
    is_approved BOOLEAN NOT NULL DEFAULT false,
    is_rejected BOOLEAN NOT NULL DEFAULT false,
    is_archived BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_tasks_updated_at
BEFORE UPDATE ON public.tasks
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_tasks_user_day ON public.tasks(user_id, day_of_week);
CREATE INDEX IF NOT EXISTS idx_tasks_pending_approval ON public.tasks(user_id, is_completed, is_approved, is_rejected) 
    WHERE (photo_proof_url IS NOT NULL OR is_completed = true) AND is_approved = false AND is_rejected = false;
CREATE INDEX IF NOT EXISTS idx_tasks_approved_active ON public.tasks(user_id, is_approved, is_archived) 
    WHERE is_approved = true AND is_archived = false;

-- ==============================================================================
-- 6. EXAM RESULTS & SUBJECT RESULTS (Mapped to Room ExamResultEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.exam_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    date VARCHAR(64) NOT NULL,
    education_level education_level NOT NULL,
    total_net NUMERIC(6, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_exam_results_updated_at
BEFORE UPDATE ON public.exam_results
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_exam_results_user ON public.exam_results(user_id, created_at DESC);

CREATE TABLE IF NOT EXISTS public.subject_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    exam_id UUID NOT NULL REFERENCES public.exam_results(id) ON DELETE CASCADE,
    subject_name VARCHAR(128) NOT NULL,
    correct INTEGER NOT NULL DEFAULT 0 CHECK (correct >= 0),
    wrong INTEGER NOT NULL DEFAULT 0 CHECK (wrong >= 0),
    blank INTEGER NOT NULL DEFAULT 0 CHECK (blank >= 0),
    net NUMERIC(5, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_subject_results_exam ON public.subject_results(exam_id);

-- ==============================================================================
-- 7. XP LEDGER (Mapped to Room XPTransactionEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.xp_ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    amount INTEGER NOT NULL,
    type xp_transaction_type NOT NULL,
    reason VARCHAR(255) NOT NULL,
    source_reference VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_xp_source_reference 
ON public.xp_ledger(user_id, source_reference) 
WHERE source_reference IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_xp_ledger_user_date ON public.xp_ledger(user_id, created_at DESC);

-- ==============================================================================
-- 8. POMODORO SESSIONS (Mapped to Room PomodoroSessionEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.pomodoro_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    mode VARCHAR(64) NOT NULL DEFAULT 'FOCUS',
    subject VARCHAR(128) NOT NULL DEFAULT 'Genel Odaklanma',
    session_timestamp BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pomodoro_student ON public.pomodoro_sessions(student_id, created_at DESC);

-- ==============================================================================
-- 9. MESSAGES (Mapped to Room MessageEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation 
ON public.messages(LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), created_at ASC);
CREATE INDEX IF NOT EXISTS idx_messages_unread ON public.messages(receiver_id, is_read) WHERE is_read = false;

-- ==============================================================================
-- 10. NOTIFICATIONS (Mapped to Room NotificationEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_id VARCHAR(128),
    is_read BOOLEAN NOT NULL DEFAULT false,
    navigation_route VARCHAR(255),
    idempotency_key VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_notifications_idempotency 
ON public.notifications(recipient_id, idempotency_key);
CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON public.notifications(recipient_id, is_read, created_at DESC);

-- ==============================================================================
-- 11. REWARDS & REWARD REQUESTS (Mapped to Room RewardEntity & RewardRequestEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.rewards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    xp_cost INTEGER NOT NULL CHECK (xp_cost > 0),
    stock INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    image_url TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_rewards_updated_at
BEFORE UPDATE ON public.rewards
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS public.reward_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    reward_id UUID NOT NULL REFERENCES public.rewards(id) ON DELETE CASCADE,
    xp_cost INTEGER NOT NULL CHECK (xp_cost > 0),
    status reward_request_status NOT NULL DEFAULT 'PENDING',
    processed_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    processed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_reward_requests_updated_at
BEFORE UPDATE ON public.reward_requests
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_reward_requests_student ON public.reward_requests(student_id, status);
CREATE INDEX IF NOT EXISTS idx_reward_requests_pending ON public.reward_requests(status) WHERE status = 'PENDING';

-- ==============================================================================
-- 12. PARENT MEETINGS (Mapped to Room ParentMeetingEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.parent_meetings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    coach_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    date VARCHAR(64) NOT NULL,
    topic VARCHAR(255) NOT NULL DEFAULT 'Genel Gelişim',
    notes TEXT NOT NULL DEFAULT '',
    decisions TEXT NOT NULL DEFAULT '',
    next_meeting_date VARCHAR(64) DEFAULT '',
    subject_notes_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    is_completed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_parent_meetings_updated_at
BEFORE UPDATE ON public.parent_meetings
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE INDEX IF NOT EXISTS idx_parent_meetings_student ON public.parent_meetings(student_id);
CREATE INDEX IF NOT EXISTS idx_parent_meetings_coach ON public.parent_meetings(coach_id);

-- ==============================================================================
-- 13. FINANCIAL TRANSACTIONS & PACKAGE CONFIGS (Mapped to Expense, Income, PackageConfig)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.package_configs (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price VARCHAR(64) NOT NULL,
    yearly_price VARCHAR(64) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    student_capacity INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_package_configs_updated_at
BEFORE UPDATE ON public.package_configs
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TABLE IF NOT EXISTS public.financial_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(16) NOT NULL CHECK (transaction_type IN ('INCOME', 'EXPENSE')),
    user_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    package_id VARCHAR(64) REFERENCES public.package_configs(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    category VARCHAR(128) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(8) NOT NULL DEFAULT 'TRY',
    date VARCHAR(64) NOT NULL,
    description TEXT,
    created_by_admin_id UUID NOT NULL REFERENCES public.profiles(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_financial_type ON public.financial_transactions(transaction_type, created_at DESC);

-- ==============================================================================
-- 14. REMINDER SETTINGS (Mapped to Room ReminderSettingsEntity)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.reminder_settings (
    user_id UUID PRIMARY KEY REFERENCES public.profiles(id) ON DELETE CASCADE,
    notifications_enabled BOOLEAN NOT NULL DEFAULT true,
    daily_reminder_enabled BOOLEAN NOT NULL DEFAULT true,
    daily_reminder_time VARCHAR(16) NOT NULL DEFAULT '18:00',
    second_daily_reminder_enabled BOOLEAN NOT NULL DEFAULT false,
    second_daily_reminder_time VARCHAR(16) NOT NULL DEFAULT '21:00',
    task_reminders_enabled BOOLEAN NOT NULL DEFAULT true,
    incomplete_task_reminder_enabled BOOLEAN NOT NULL DEFAULT true,
    weekly_plan_reminder_enabled BOOLEAN NOT NULL DEFAULT true,
    weekly_plan_reminder_day SMALLINT NOT NULL DEFAULT 1 CHECK (weekly_plan_reminder_day BETWEEN 1 AND 7),
    weekly_plan_reminder_time VARCHAR(16) NOT NULL DEFAULT '18:00',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_reminder_settings_updated_at
BEFORE UPDATE ON public.reminder_settings
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- ==============================================================================
-- 15. FILE METADATA TABLE (Cloudflare R2 Object Reference)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.file_metadata (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    related_entity_type VARCHAR(64) NOT NULL, -- 'avatar', 'task_proof', 'student_report'
    related_entity_id VARCHAR(128),
    bucket_name VARCHAR(128) NOT NULL DEFAULT 'mahfaza-assets-private',
    object_key TEXT NOT NULL UNIQUE,
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    file_size_bytes BIGINT NOT NULL CHECK (file_size_bytes > 0),
    sha256_hash VARCHAR(64),
    access_level VARCHAR(32) NOT NULL DEFAULT 'PRIVATE' CHECK (access_level IN ('PRIVATE', 'COACH_STUDENT_RESTRICTED', 'PUBLIC')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_file_owner ON public.file_metadata(owner_id);
CREATE INDEX IF NOT EXISTS idx_file_entity ON public.file_metadata(related_entity_type, related_entity_id);

-- ==============================================================================
-- 16. SYNC OPERATIONS / IDEMPOTENCY QUEUE (Mapped to Offline-First Queue)
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.sync_operations (
    operation_id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    operation_type VARCHAR(16) NOT NULL CHECK (operation_type IN ('INSERT', 'UPDATE', 'DELETE')),
    payload JSONB NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL UNIQUE,
    status sync_operation_status NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error TEXT,
    client_timestamp BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    applied_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_sync_user_status ON public.sync_operations(user_id, status);
