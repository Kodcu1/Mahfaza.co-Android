package com.example.model

import kotlinx.serialization.Serializable

@Serializable
enum class EducationLevel(val displayName: String) {
    LGS_8TH("LGS (8. Sınıf)"),
    YKS_TYT_AYT("YKS (TYT-AYT)"),
    KPSS("KPSS")
}

@Serializable
enum class UserRole {
    STUDENT, COACH, ADMIN
}

@Serializable
data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val educationLevel: EducationLevel? = null,
    val packageId: String,
    val currentXp: Int = 0,
    val lifetimeXp: Int = 0,
    val streak: Int = 0,
    val studentCapacity: Int = 0, // For Coaches
    val assignedStudents: List<String> = emptyList(), // For Coaches (Student IDs)
    
    // Detailed Coach Profile Fields
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
    
    // Coach Code and Student Target Academic Fields
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
data class PomodoroSession(
    val id: String,
    val studentId: String,
    val durationMinutes: Int,
    val timestamp: Long,
    val mode: String = "FOCUS",
    val subject: String = "Genel Odaklanma"
)

@Serializable
data class EducationEntry(
    val id: String,
    val university: String,
    val department: String,
    val degree: String,
    val graduationYear: String
)

@Serializable
data class ExperienceEntry(
    val id: String,
    val title: String,
    val company: String,
    val period: String
)

@Serializable
data class CertificateEntry(
    val id: String,
    val name: String,
    val organization: String,
    val year: String
)

@Serializable
enum class Entitlement(val displayName: String) {
    DAILY_TASKS("Günlük Görev Takibi"),
    XP_TRACKING("XP ve Rozet Sistemi"),
    EXAM_CALCULATION("Deneme Analizi"),
    WEEKLY_PROGRAM_VIEW("Haftalık Program Görüntüleme"),
    ADVANCED_ANALYTICS("Gelişmiş Performans Analizi"),
    EXAM_HISTORY("Deneme Sınavı Geçmişi"),
    SUBJECT_ANALYSIS("Branş Bazlı Konu Analizi"),
    AI_PLANNER_STUDENT("Yapay Zeka Destekli Öğrenci Planlayıcı"),
    COACH_PORTFOLIO("Koç Portföyü"),
    TASK_ASSIGNMENT("Öğrenciye Görev Atama"),
    AI_PLANNER_COACH("Yapay Zeka Destekli Koçluk Planlayıcı"),
    TEAM_MANAGEMENT("Ekip ve Kurum Yönetimi")
}

@Serializable
data class Package(
    val id: String,
    val name: String,
    val price: String,
    val yearlyPrice: String = "",
    val role: UserRole,
    val entitlements: List<Entitlement>,
    val studentCapacity: Int = 0,
    val isFree: Boolean = false,
    val features: List<String> = emptyList(),
    val limits: List<String> = emptyList()
)

@Serializable
data class Task(
    val id: String,
    val title: String,
    val subject: String,
    val topic: String,
    val targetQuestionCount: Int,
    val completedQuestionCount: Int = 0,
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val xpReward: Int = 10,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun)
    val reminderTime: String? = null, // "HH:mm"
    val photoProofUri: String? = null,
    val isApproved: Boolean = false,
    val isRejected: Boolean = false
)

@Serializable
enum class TaskPriority {
    LOW, MEDIUM, HIGH
}

@Serializable
data class ExamResult(
    val id: String,
    val name: String,
    val date: String,
    val educationLevel: EducationLevel,
    val subjectResults: List<SubjectResult>
)

@Serializable
data class SubjectResult(
    val subjectName: String,
    val correct: Int,
    val wrong: Int,
    val blank: Int,
    val net: Double
)

@Serializable
enum class XPTransactionType {
    EARNED, SPENT, ADJUSTED
}

@Serializable
data class XPTransaction(
    val id: String,
    val studentId: String,
    val amount: Int,
    val type: XPTransactionType,
    val reason: String,
    val timestamp: Long,
    val sourceReference: String? = null
)

@Serializable
enum class NotificationType {
    TASK_ASSIGNED,
    STUDY_SUBMITTED,
    STUDY_APPROVED,
    STUDY_REJECTED,
    EXAM_SAVED,
    EXAM_REVIEWED,
    REWARD_REQUESTED,
    REWARD_STATUS,
    LEVEL_UP,
    BADGE_UNLOCKED,
    NEW_MESSAGE,
    SYSTEM_ANNOUNCEMENT
}

@Serializable
data class AppNotification(
    val id: String,
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
data class Reward(
    val id: String,
    val name: String,
    val description: String,
    val xpCost: Int,
    val stock: Int,
    val imageUrl: String? = null
)

@Serializable
enum class RewardRequestStatus {
    PENDING, APPROVED, REJECTED
}

@Serializable
data class RewardRequest(
    val id: String,
    val studentId: String,
    val studentName: String,
    val rewardId: String,
    val rewardName: String,
    val xpCost: Int,
    val timestamp: Long,
    val status: RewardRequestStatus = RewardRequestStatus.PENDING
)

@Serializable
data class ChatMessage(
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

@Serializable
data class Income(
    val id: String,
    val userId: String,
    val amount: Double,
    val date: String,
    val description: String,
    val packageId: String,
    val createdByAdminId: String
)

@Serializable
data class PackageConfig(
    val id: String,
    val name: String,
    val price: String,
    val yearlyPrice: String,
    val isActive: Boolean = true,
    val studentCapacity: Int = 0
)

@Serializable
data class Expense(
    val id: String,
    val title: String,
    val category: String,
    val amount: Double,
    val currency: String = "TRY",
    val date: String,
    val description: String,
    val createdByAdminId: String
)

object PackageCatalog {
    // Student Packages
    val STUDENT_STANDART = Package(
        id = "std_free",
        name = "Ücretsiz Başlangıç",
        price = "0 TL",
        yearlyPrice = "0 TL",
        role = UserRole.STUDENT,
        isFree = true,
        entitlements = listOf(
            Entitlement.DAILY_TASKS,
            Entitlement.XP_TRACKING,
            Entitlement.EXAM_CALCULATION,
            Entitlement.WEEKLY_PROGRAM_VIEW
        ),
        features = listOf(
            "Profil ve Akademik Hedefler",
            "Temel Günlük Görev Listesi",
            "Temel Deneme Sınavı Girişi",
            "Temel XP & Rozet Sistemi"
        ),
        limits = listOf(
            "Gelişmiş branş analizleri kilitli",
            "Soru çözüm geçmişi sınırlı"
        )
    )
    
    val STUDENT_PLUS = Package(
        id = "std_plus",
        name = "Mahfaza Plus",
        price = "99,99 TL/ay",
        yearlyPrice = "999 TL/yıl",
        role = UserRole.STUDENT,
        entitlements = listOf(
            Entitlement.DAILY_TASKS,
            Entitlement.XP_TRACKING,
            Entitlement.EXAM_CALCULATION,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.ADVANCED_ANALYTICS,
            Entitlement.EXAM_HISTORY,
            Entitlement.SUBJECT_ANALYSIS
        ),
        features = listOf(
            "Tüm Ücretsiz Başlangıç Özellikleri",
            "Tam Haftalık Çalışma Programı",
            "Fotoğraflı Kanıt & Görev Geçmişi",
            "Branş Bazlı Ayrıntılı Net Analizi",
            "Koç ile Mesajlaşma Modülü",
            "Genişletilmiş Başarı & Rozet Takibi"
        )
    )
    
    val STUDENT_PRO = Package(
        id = "std_pro",
        name = "Mahfaza Pro",
        price = "149,99 TL/ay",
        yearlyPrice = "1.499 TL/yıl",
        role = UserRole.STUDENT,
        entitlements = listOf(
            Entitlement.DAILY_TASKS,
            Entitlement.XP_TRACKING,
            Entitlement.EXAM_CALCULATION,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.ADVANCED_ANALYTICS,
            Entitlement.EXAM_HISTORY,
            Entitlement.SUBJECT_ANALYSIS,
            Entitlement.AI_PLANNER_STUDENT
        ),
        features = listOf(
            "Tüm Mahfaza Plus Özellikleri",
            "Gelişmiş Performans & Trend Analizleri",
            "Ayrıntılı PDF Karne & Çalışma Raporları",
            "Yapay Zeka Destekli Çalışma Planlayıcı",
            "Öncelikli Koçluk & Ödül Talepleri"
        )
    )

    // Coach Packages
    val COACH_START = Package(
        id = "coach_start",
        name = "Koç Başlangıç",
        price = "159 TL/ay",
        yearlyPrice = "1.590 TL/yıl",
        role = UserRole.COACH,
        isFree = false,
        studentCapacity = 3,
        entitlements = listOf(
            Entitlement.COACH_PORTFOLIO,
            Entitlement.TASK_ASSIGNMENT,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.EXAM_CALCULATION
        ),
        features = listOf(
            "En Fazla 3 Aktif Öğrenci",
            "Görev Atama & Onay Merkezi",
            "Öğrenci Haftalık Takip Çizelgesi",
            "Veli Görüşmeleri Yönetimi"
        ),
        limits = listOf("3 aktif öğrenci sınırı")
    )

    val COACH_PLUS = Package(
        id = "coach_plus",
        name = "Koç Plus",
        price = "239 TL/ay",
        yearlyPrice = "2.390 TL/yıl",
        role = UserRole.COACH,
        isFree = false,
        studentCapacity = 5,
        entitlements = listOf(
            Entitlement.COACH_PORTFOLIO,
            Entitlement.TASK_ASSIGNMENT,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.EXAM_CALCULATION,
            Entitlement.ADVANCED_ANALYTICS
        ),
        features = listOf(
            "En Fazla 5 Aktif Öğrenci",
            "Fotoğraflı Kanıt İnceleme",
            "Öğrenci 360° Analiz Raporları",
            "Veli PDF Rapor Dışa Aktarma"
        ),
        limits = listOf("5 aktif öğrenci sınırı")
    )
    
    val COACH_STANDART = Package(
        id = "coach_std",
        name = "Koç Standart",
        price = "429 TL/ay",
        yearlyPrice = "4.290 TL/yıl",
        role = UserRole.COACH,
        studentCapacity = 10,
        entitlements = listOf(
            Entitlement.COACH_PORTFOLIO,
            Entitlement.TASK_ASSIGNMENT,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.EXAM_CALCULATION,
            Entitlement.ADVANCED_ANALYTICS,
            Entitlement.EXAM_HISTORY
        ),
        features = listOf(
            "En Fazla 10 Aktif Öğrenci",
            "Tüm Koç Plus Özellikleri",
            "Gelişmiş Deneme Trend Grafikleri",
            "Grup Performans Takibi"
        ),
        limits = listOf("10 aktif öğrenci sınırı")
    )
    
    val COACH_PRO = Package(
        id = "coach_pro",
        name = "Koç Pro",
        price = "579 TL/ay",
        yearlyPrice = "5.790 TL/yıl",
        role = UserRole.COACH,
        studentCapacity = 25,
        entitlements = listOf(
            Entitlement.COACH_PORTFOLIO,
            Entitlement.TASK_ASSIGNMENT,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.EXAM_CALCULATION,
            Entitlement.ADVANCED_ANALYTICS,
            Entitlement.EXAM_HISTORY,
            Entitlement.AI_PLANNER_COACH,
            Entitlement.SUBJECT_ANALYSIS
        ),
        features = listOf(
            "En Fazla 25 Aktif Öğrenci",
            "Yapay Zeka Destekli Koçluk Planlama",
            "Özel Ödül Mağazası Yönetimi",
            "Otomatik Haftalık Raporlama"
        ),
        limits = listOf("25 aktif öğrenci sınırı")
    )

    val COACH_EXPERT = Package(
        id = "coach_expert",
        name = "Koç Uzman",
        price = "579 TL/ay",
        yearlyPrice = "5.790 TL/yıl",
        role = UserRole.COACH,
        studentCapacity = 15,
        entitlements = listOf(
            Entitlement.COACH_PORTFOLIO,
            Entitlement.TASK_ASSIGNMENT,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.EXAM_CALCULATION,
            Entitlement.ADVANCED_ANALYTICS,
            Entitlement.EXAM_HISTORY,
            Entitlement.AI_PLANNER_COACH,
            Entitlement.SUBJECT_ANALYSIS
        ),
        features = listOf(
            "En Fazla 15 Aktif Öğrenci",
            "Yapay Zeka Destekli Koçluk Planlama",
            "Özel Ödül Mağazası Yönetimi"
        ),
        limits = listOf("15 aktif öğrenci sınırı")
    )

    val COACH_TEAM = Package(
        id = "coach_team",
        name = "Koç Ekip",
        price = "739 TL/ay",
        yearlyPrice = "7.390 TL/yıl",
        role = UserRole.COACH,
        studentCapacity = 20,
        entitlements = listOf(
            Entitlement.COACH_PORTFOLIO,
            Entitlement.TASK_ASSIGNMENT,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.EXAM_CALCULATION,
            Entitlement.ADVANCED_ANALYTICS,
            Entitlement.EXAM_HISTORY,
            Entitlement.AI_PLANNER_COACH,
            Entitlement.SUBJECT_ANALYSIS,
            Entitlement.TEAM_MANAGEMENT
        ),
        features = listOf(
            "En Fazla 20 Aktif Öğrenci",
            "Ekip & Asistan Koç Yönetimi",
            "Kurumsal Şablonlar & Görev Havuzu"
        ),
        limits = listOf("20 aktif öğrenci sınırı")
    )
    
    val COACH_INSTITUTION = Package(
        id = "coach_inst",
        name = "Koç Kurumsal",
        price = "999 TL/ay",
        yearlyPrice = "9.990 TL/yıl",
        role = UserRole.COACH,
        studentCapacity = 50,
        entitlements = listOf(
            Entitlement.COACH_PORTFOLIO,
            Entitlement.TASK_ASSIGNMENT,
            Entitlement.WEEKLY_PROGRAM_VIEW,
            Entitlement.EXAM_CALCULATION,
            Entitlement.ADVANCED_ANALYTICS,
            Entitlement.EXAM_HISTORY,
            Entitlement.AI_PLANNER_COACH,
            Entitlement.SUBJECT_ANALYSIS,
            Entitlement.TEAM_MANAGEMENT
        ),
        features = listOf(
            "En Fazla 50 Aktif Öğrenci",
            "Sınırsız Asistan & Ekip Yönetimi",
            "Kurumsal Markalama & Özel Raporlar",
            "Öncelikli Kurumsal Destek"
        ),
        limits = listOf("50 aktif öğrenci sınırı")
    )

    fun getPackageById(id: String): Package? {
        return (getAllStudentPackages() + getAllCoachPackages()).find { it.id == id }
    }

    fun getAllStudentPackages() = listOf(STUDENT_STANDART, STUDENT_PLUS, STUDENT_PRO)
    fun getAllCoachPackages() = listOf(COACH_START, COACH_PLUS, COACH_STANDART, COACH_EXPERT, COACH_TEAM, COACH_PRO, COACH_INSTITUTION)
}

@Serializable
data class ParentMeeting(
    val id: String,
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

