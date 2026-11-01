package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.ExpenseEntity
import com.example.data.local.PackageConfigEntity
import com.example.data.local.UserEntity
import com.example.data.local.TaskEntity
import com.example.data.local.ExamResultEntity
import com.example.data.local.MessageEntity
import com.example.data.local.XPTransactionEntity
import com.example.data.local.RewardEntity
import com.example.data.local.RewardRequestEntity
import com.example.data.local.ParentMeetingEntity
import com.example.data.local.PomodoroSessionEntity
import com.example.data.local.NotificationEntity
@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        ExamResultEntity::class,
        MessageEntity::class,
        XPTransactionEntity::class,
        RewardEntity::class,
        RewardRequestEntity::class,
        ParentMeetingEntity::class,
        PomodoroSessionEntity::class,
        NotificationEntity::class,
        ExpenseEntity::class,
        PackageConfigEntity::class,
        IncomeEntity::class,
        ReminderSettingsEntity::class,
        CouponEntity::class,
        CouponUsageEntity::class
    ],
    version = 14,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun examDao(): ExamDao
    abstract fun messageDao(): MessageDao
    abstract fun xpDao(): XPDao
    abstract fun rewardDao(): RewardDao
    abstract fun rewardRequestDao(): RewardRequestDao
    abstract fun parentMeetingDao(): ParentMeetingDao
    abstract fun pomodoroDao(): PomodoroDao
    abstract fun notificationDao(): NotificationDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun packageConfigDao(): PackageConfigDao
    abstract fun incomeDao(): IncomeDao
    abstract fun reminderSettingsDao(): ReminderSettingsDao
    abstract fun couponDao(): CouponDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN avatarUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE parent_meetings ADD COLUMN topic TEXT NOT NULL DEFAULT 'Genel Gelişim'")
                db.execSQL("ALTER TABLE parent_meetings ADD COLUMN decisions TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE parent_meetings ADD COLUMN nextMeetingDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE parent_meetings ADD COLUMN subjectNotesJson TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN coachCode TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE users ADD COLUMN targetUniversity TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE users ADD COLUMN targetDepartment TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE users ADD COLUMN targetRanking TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE users ADD COLUMN assignedCoachId TEXT DEFAULT NULL")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS pomodoro_sessions (
                        id TEXT NOT NULL PRIMARY KEY,
                        studentId TEXT NOT NULL,
                        durationMinutes INTEGER NOT NULL,
                        timestamp INTEGER NOT NULL,
                        mode TEXT NOT NULL DEFAULT 'FOCUS'
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN goals TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE pomodoro_sessions ADD COLUMN subject TEXT NOT NULL DEFAULT 'Genel Odaklanma'")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN targetCity TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE users ADD COLUMN targetExamDate TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE users ADD COLUMN dailyQuestionTarget INTEGER NOT NULL DEFAULT 200")
                db.execSQL("ALTER TABLE users ADD COLUMN dailyStudyTarget TEXT DEFAULT '3 Saat'")
            }
        }

        val MIGRATION_6_7_REAL = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE xp_transactions ADD COLUMN sourceReference TEXT DEFAULT NULL")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS notifications (
                        id TEXT NOT NULL PRIMARY KEY,
                        recipientId TEXT NOT NULL,
                        type TEXT NOT NULL,
                        title TEXT NOT NULL,
                        message TEXT NOT NULL,
                        relatedEntityId TEXT DEFAULT NULL,
                        timestamp INTEGER NOT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        navigationRoute TEXT DEFAULT NULL,
                        idempotencyKey TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_notifications_idempotencyKey ON notifications(idempotencyKey)")
            }
        }

        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN examTracks TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE users ADD COLUMN subscriptionPlan TEXT NOT NULL DEFAULT 'FREE'")
            }
        }

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isRejected INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS expenses (
                        id TEXT NOT NULL PRIMARY KEY,
                        title TEXT NOT NULL,
                        category TEXT NOT NULL,
                        amount REAL NOT NULL,
                        currency TEXT NOT NULL DEFAULT 'TRY',
                        date TEXT NOT NULL,
                        description TEXT NOT NULL,
                        createdByAdminId TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS package_configs (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        price TEXT NOT NULL,
                        yearlyPrice TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        studentCapacity INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS incomes (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        amount REAL NOT NULL,
                        date TEXT NOT NULL,
                        description TEXT NOT NULL,
                        packageId TEXT NOT NULL,
                        createdByAdminId TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_11_12 = object : androidx.room.migration.Migration(11, 12) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN reminderTime TEXT DEFAULT NULL")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS reminder_settings (
                        userId TEXT NOT NULL PRIMARY KEY,
                        notificationsEnabled INTEGER NOT NULL DEFAULT 1,
                        dailyReminderEnabled INTEGER NOT NULL DEFAULT 1,
                        dailyReminderTime TEXT NOT NULL DEFAULT '18:00',
                        secondDailyReminderEnabled INTEGER NOT NULL DEFAULT 0,
                        secondDailyReminderTime TEXT NOT NULL DEFAULT '21:00',
                        taskRemindersEnabled INTEGER NOT NULL DEFAULT 1,
                        incompleteTaskReminderEnabled INTEGER NOT NULL DEFAULT 1,
                        weeklyPlanReminderEnabled INTEGER NOT NULL DEFAULT 1,
                        weeklyPlanReminderDay INTEGER NOT NULL DEFAULT 1,
                        weeklyPlanReminderTime TEXT NOT NULL DEFAULT '18:00'
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_12_13 = object : androidx.room.migration.Migration(12, 13) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_13_14 = object : androidx.room.migration.Migration(13, 14) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS coupons (
                        code TEXT NOT NULL PRIMARY KEY,
                        discountType TEXT NOT NULL,
                        discountValue REAL NOT NULL,
                        startDate INTEGER NOT NULL,
                        endDate INTEGER NOT NULL,
                        totalUsageLimit INTEGER NOT NULL,
                        perUserLimit INTEGER NOT NULL DEFAULT 1,
                        usedCount INTEGER NOT NULL DEFAULT 0,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        applicablePackageId TEXT DEFAULT NULL,
                        applicableBillingPeriod TEXT DEFAULT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS coupon_usages (
                        id TEXT NOT NULL PRIMARY KEY,
                        couponCode TEXT NOT NULL,
                        userId TEXT NOT NULL,
                        usedAt INTEGER NOT NULL,
                        packageId TEXT DEFAULT NULL,
                        discountAmount REAL NOT NULL DEFAULT 0.0
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mahfaza_database"
                )
                    .addMigrations(
                        MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, 
                        MIGRATION_6_7_REAL, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, 
                        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

