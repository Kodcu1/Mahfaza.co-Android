# Mahfaza.co V34.1 — PostgreSQL Production Schema Blueprint

## 1. Genel Mimari Özeti

Mahfaza.co V34.1, mobil cihazda mevcut Room SQLite V13 veritabanını çevrimdışı önbellek olarak korurken, bulut tarafında Supabase PostgreSQL veritabanını tek otoriter veri kaynağı (Single Source of Truth) olarak konumlandırır.

- **Veritabanı Motoru:** PostgreSQL 15+ (Supabase)
- **Kimlik Doğrulama:** Supabase Auth (`auth.users`)
- **İlişki Modeli:** `public.profiles.id` doğrudan `auth.users.id` ile birebir (1:1) bağlıdır ve `ON DELETE CASCADE` ile yönetilir.
- **Güvenlik Modeli:** Tüm tablolarda Row Level Security (RLS) etkinleştirilmiştir.

---

## 2. Room V13 Varlıklarından PostgreSQL Tablolarına Eşleme

| No | Room V13 Varlığı | PostgreSQL Tablosu | Açıklama & Dönüşüm Notları |
|:---|:---|:---|:---|
| 1 | `UserEntity` | `public.profiles` | `auth.users` ile 1:1 bağlı. JSON listeleri (`education`, `experience`, `expertiseTags`) `jsonb` olarak tutulur. |
| 2 | `TaskEntity` | `public.tasks` | Görevler, soru sayıları, onay (`is_approved`) ve arşiv (`is_archived`) durumu. |
| 3 | `ExamResultEntity` | `public.exam_results` & `public.subject_results` | Deneme sınavı genel verisi ile ders bazlı netler ilişkisel alt tabloya ayrıştırılmıştır. |
| 4 | `XPTransactionEntity` | `public.xp_ledger` | Değiştirilemez (append-only) XP defteri. `source_reference` üzerinde `UNIQUE` kısıt bulunur. |
| 5 | `PomodoroSessionEntity` | `public.pomodoro_sessions` | Odaklanma kayıtları ve çalışma süreleri. |
| 6 | `MessageEntity` | `public.messages` | Öğrenci ve koç arasındaki şifreli/izole mesajlaşma kayıtları. |
| 7 | `NotificationEntity` | `public.notifications` | `idempotency_key` ile tekilleştirilmiş bildirim kuyruğu. |
| 8 | `RewardEntity` | `public.rewards` | Puan kataloğu ve mevcut stok durumu. |
| 9 | `RewardRequestEntity` | `public.reward_requests` | Öğrenci ödül talepleri ve koç onay durumu. |
| 10 | `ParentMeetingEntity` | `public.parent_meetings` | Veli görüşme kayıtları ve kararları (öğrenci erişimine kapalı). |
| 11 | `ExpenseEntity` & `IncomeEntity` | `public.financial_transactions` | Muhasebe gelir/gider kayıtları tek birleştirilmiş finans tablosunda toplanmıştır. |
| 12 | `PackageConfigEntity` | `public.package_configs` | Abonelik paketleri, fiyatlar ve öğrenci limitleri. |
| 13 | `ReminderSettingsEntity` | `public.reminder_settings` | Kullanıcının bildirim tercihleri ve alarm saatleri. |
| 14 | *(Yeni Altyapı)* | `public.invitations` | SHA-256 hash ile korunan tek kullanımlık davet kodları. |
| 15 | *(Yeni Altyapı)* | `public.file_metadata` | Cloudflare R2 üzerindeki dosya anahtarları ve erişim izinleri. |
| 16 | *(Yeni Altyapı)* | `public.sync_operations` | Çevrimdışı eşitleme kuyruğu ve işlem geçmişi. |
| 17 | *(Yeni Altyapı)* | `public.system_settings` | Sistem konfigürasyonu (günlük XP tavanı, bootstrap kilidi). RLS korumalı. |

---

## 3. Tablo Detayları ve Kısıtlar (Constraints)

### 3.1. `public.profiles`
- `id`: `UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE`
- `role`: `user_role NOT NULL DEFAULT 'STUDENT'` ('STUDENT', 'COACH', 'ADMIN')
- `email`: `VARCHAR(255) NOT NULL UNIQUE`
- `current_xp`: `INTEGER NOT NULL DEFAULT 0 CHECK (current_xp >= 0)`
- `lifetime_xp`: `INTEGER NOT NULL DEFAULT 0 CHECK (lifetime_xp >= current_xp)`
- `streak`: `INTEGER NOT NULL DEFAULT 0 CHECK (streak >= 0)`
- `coach_code`: `VARCHAR(32) UNIQUE` (Yalnızca koçlar için üretilir)
- `assigned_coach_id`: `UUID REFERENCES public.profiles(id) ON DELETE SET NULL`
- `created_at`, `updated_at`: `TIMESTAMPTZ NOT NULL DEFAULT NOW()`

### 3.2. `public.tasks`
- `id`: `UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `user_id`: `UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE`
- `created_by`: `UUID REFERENCES public.profiles(id) ON DELETE SET NULL`
- `target_question_count`: `INTEGER NOT NULL DEFAULT 10 CHECK (target_question_count >= 0)`
- `completed_question_count`: `INTEGER NOT NULL DEFAULT 0 CHECK (completed_question_count >= 0)`
- `day_of_week`: `SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7)`
- `is_approved`, `is_rejected`, `is_archived`: `BOOLEAN NOT NULL DEFAULT false`

### 3.3. `public.xp_ledger`
- `id`: `UUID PRIMARY KEY DEFAULT gen_random_uuid()`
- `user_id`: `UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE`
- `amount`: `INTEGER NOT NULL` (Kazanılan pozitif, harcanan negatif)
- `type`: `xp_transaction_type NOT NULL` ('EARNED', 'SPENT', 'ADJUSTED')
- `source_reference`: `VARCHAR(128)`
- **Unique Kısıt:** `uq_xp_source_reference (user_id, source_reference) WHERE source_reference IS NOT NULL` — Mükerrer XP yazılmasını kesinlikle önler.

---

## 4. Veri Türleri ve Zaman Damgası Standartları

1. **Zaman Damgaları:** Tüm zamanlar `TIMESTAMPTZ` (UTC) olarak saklanır. Room tarafındaki `Long` epoch millis değerleri ISO-8601 string veya UTC saniye olarak çift yönlü dönüştürülür.
2. **Finansal Tutarlar:** Float yerine `NUMERIC(10, 2)` kullanılarak yuvarlama hataları engellenmiştir.
3. **Net Hesaplamaları:** Deneme sınavı netleri `NUMERIC(5, 2)` hassasiyetinde tutulur.
