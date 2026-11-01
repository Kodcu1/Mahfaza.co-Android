package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.*
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val educationLevel: EducationLevel? = null,
    val packageId: String,
    val currentXp: Int = 0,
    val lifetimeXp: Int = 0,
    val streak: Int = 0,
    val studentCapacity: Int = 0,
    val assignedStudents: List<String> = emptyList(),
    val title: String? = null,
    val bio: String? = null,
    val coachingApproach: String? = null,
    val expertiseTags: List<String> = emptyList(),
    val education: List<EducationEntry> = emptyList(),
    val experience: List<ExperienceEntry> = emptyList(),
    val certificates: List<CertificateEntry> = emptyList(),
    val workingDays: List<String> = emptyList(),
    val workingHours: String? = null,
    val acceptingStudents: Boolean = true,
    val avatarUri: String? = null,
    val coachCode: String? = null,
    val targetUniversity: String? = null,
    val targetDepartment: String? = null,
    val targetRanking: String? = null,
    val targetCity: String? = null,
    val targetExamDate: String? = null,
    val dailyQuestionTarget: Int = 200,
    val dailyStudyTarget: String? = "3 Saat",
    val goals: String? = null,
    val assignedCoachId: String? = null,
    val examTracks: List<String> = emptyList(),
    val subscriptionPlan: String = "FREE"
)

@Serializable
@Entity(tableName = "pomodoro_sessions")
data class PomodoroSessionEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val durationMinutes: Int,
    val timestamp: Long,
    val mode: String = "FOCUS",
    val subject: String = "Genel Odaklanma"
)

@Serializable
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val subject: String,
    val topic: String,
    val targetQuestionCount: Int,
    val completedQuestionCount: Int = 0,
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val xpReward: Int = 10,
    val dayOfWeek: Int,
    val reminderTime: String? = null, // "HH:mm"
    val photoProofUri: String? = null,
    val isApproved: Boolean = false,
    val isRejected: Boolean = false,
    val isArchived: Boolean = false
)

@Serializable
@Entity(tableName = "exam_results")
data class ExamResultEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val date: String,
    val educationLevel: EducationLevel,
    val subjectResults: List<SubjectResult>
)

@Serializable
@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Serializable
@Entity(tableName = "xp_transactions")
data class XPTransactionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Int,
    val type: XPTransactionType,
    val reason: String,
    val timestamp: Long,
    val sourceReference: String? = null
)

@Serializable
@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val recipientId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val relatedEntityId: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false,
    val navigationRoute: String? = null,
    val idempotencyKey: String
)

@Serializable
@Entity(tableName = "rewards")
data class RewardEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val xpCost: Int,
    val stock: Int,
    val imageUrl: String? = null
)

@Serializable
@Entity(tableName = "reward_requests")
data class RewardRequestEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val rewardId: String,
    val rewardName: String,
    val xpCost: Int,
    val timestamp: Long,
    val status: RewardRequestStatus
)

@Serializable
@Entity(tableName = "parent_meetings")
data class ParentMeetingEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val date: String,
    val notes: String,
    val isCompleted: Boolean = false,
    val topic: String = "Genel Gelişim",
    val decisions: String = "",
    val nextMeetingDate: String = "",
    val subjectNotesJson: String = ""
)

@Serializable
@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val currency: String = "TRY",
    val date: String,
    val description: String,
    val createdByAdminId: String
)

@Serializable
@Entity(tableName = "package_configs")
data class PackageConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val price: String,
    val yearlyPrice: String,
    val isActive: Boolean = true,
    val studentCapacity: Int = 0
)

@Serializable
@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val amount: Double,
    val date: String,
    val description: String,
    val packageId: String,
    val createdByAdminId: String
)

@Serializable
@Entity(tableName = "reminder_settings")
data class ReminderSettingsEntity(
    @PrimaryKey val userId: String,
    val notificationsEnabled: Boolean = true,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderTime: String = "18:00",
    val secondDailyReminderEnabled: Boolean = false,
    val secondDailyReminderTime: String = "21:00",
    val taskRemindersEnabled: Boolean = true,
    val incompleteTaskReminderEnabled: Boolean = true,
    val weeklyPlanReminderEnabled: Boolean = true,
    val weeklyPlanReminderDay: Int = 1, // Sunday
    val weeklyPlanReminderTime: String = "18:00"
)

@Serializable
@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey val code: String,
    val discountType: String, // "PERCENTAGE" or "FIXED_TRY"
    val discountValue: Double,
    val startDate: Long,
    val endDate: Long,
    val totalUsageLimit: Int,
    val perUserLimit: Int = 1,
    val usedCount: Int = 0,
    val isActive: Boolean = true,
    val applicablePackageId: String? = null,
    val applicableBillingPeriod: String? = null // "MONTHLY", "YEARLY"
)

@Serializable
@Entity(tableName = "coupon_usages")
data class CouponUsageEntity(
    @PrimaryKey val id: String,
    val couponCode: String,
    val userId: String,
    val usedAt: Long,
    val packageId: String? = null,
    val discountAmount: Double = 0.0
)

