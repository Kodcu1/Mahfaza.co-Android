package com.example.navigation

import kotlinx.serialization.Serializable

@Serializable
object Splash

@Serializable
object Login

@Serializable
data class Register(val role: String = "STUDENT", val packageId: String = "std_free")

@Serializable
data class PackageSelection(val role: String)

@Serializable
data class Payment(val packageId: String, val role: String)

@Serializable
data class StudentDashboard(val educationLevel: String)

@Serializable
object WeeklyProgram

@Serializable
object ExamResults

@Serializable
object Profile

@Serializable
object CoachPanel

@Serializable
object StudentPortfolio

@Serializable
object AdminPanel

@Serializable
object SelectLevel

@Serializable
object AiPlannerDraft

@Serializable
data class FeatureLocked(val featureName: String, val requiredPackageName: String)

@Serializable
object ApprovalCenter

@Serializable
object CoachingTasks

@Serializable
object RewardManagement

@Serializable
object ParentMeetings

@Serializable
object AnalyticsReport

@Serializable
object Messages

@Serializable
data class StudentDetail(val studentId: String)

@Serializable
data class Chat(val studentId: String)

@Serializable
object RewardStore

@Serializable
object PomodoroFocus

@Serializable
object Achievements

@Serializable
object CoachInfo

@Serializable
object Settings

@Serializable
object Notifications

@Serializable
object AdminUserManagement

@Serializable
object AdminPackageManagement

@Serializable
object AdminFinancialReports

@Serializable
object AdminBackup

@Serializable
object AdminCouponManagement

@Serializable
object ReminderSettings

