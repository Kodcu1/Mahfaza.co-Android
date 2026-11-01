package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object MockRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _exams = MutableStateFlow<List<ExamResult>>(emptyList())
    val exams: StateFlow<List<ExamResult>> = _exams.asStateFlow()

    private val _registeredUsers = MutableStateFlow<List<User>>(emptyList())

    init {
        // Seed some initial users
        _registeredUsers.value = listOf(
            User("admin-1", "Admin", "admin@mahfaza.com", UserRole.ADMIN, null, "admin_pkg"),
            User(
                id = "s1",
                name = "Ahmet Yılmaz",
                email = "ahmet@test.com",
                role = UserRole.STUDENT,
                educationLevel = EducationLevel.LGS_8TH,
                packageId = PackageCatalog.STUDENT_STANDART.id,
                currentXp = 450,
                lifetimeXp = 450,
                streak = 5
            ),
            User(
                id = "c1",
                name = "Hakan Hoca",
                email = "hakan@test.com",
                role = UserRole.COACH,
                packageId = PackageCatalog.COACH_START.id,
                studentCapacity = 3,
                assignedStudents = listOf("s1"),
                title = "Eğitim Koçu & Matematik Uzmanı",
                bio = "10 yıllık eğitim tecrübemle öğrencilerin potansiyellerini keşfetmelerine yardımcı oluyorum.",
                coachingApproach = "Bireysel farklılıklara odaklanan, disiplinli ve sonuç odaklı koçluk yaklaşımı.",
                expertiseTags = listOf("LGS", "Matematik", "Eğitim Koçluğu", "Deneme Analizi"),
                education = listOf(
                    EducationEntry(UUID.randomUUID().toString(), "Boğaziçi Üniversitesi", "Matematik Öğretmenliği", "Lisans", "2014")
                ),
                experience = listOf(
                    ExperienceEntry(UUID.randomUUID().toString(), "Kıdemli Koç", "Mahfaza.co", "2020 - Günümüz"),
                    ExperienceEntry(UUID.randomUUID().toString(), "Matematik Öğretmeni", "Özel Fen Bilimleri", "2014 - 2020")
                ),
                certificates = listOf(
                    CertificateEntry(UUID.randomUUID().toString(), "Uluslararası Koçluk Sertifikası", "ICF", "2018")
                ),
                workingDays = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma"),
                workingHours = "09:00 - 18:00",
                acceptingStudents = true
            )
        )
    }

    fun login(email: String) {
        val user = _registeredUsers.value.find { it.email.equals(email, ignoreCase = true) }
        if (user != null) {
            _currentUser.value = user
            if (user.role == UserRole.STUDENT) seedStudentData()
        }
    }

    fun simulateLogin(role: UserRole, level: EducationLevel? = null, packageId: String? = null) {
        if (role == UserRole.COACH) {
            // Find our seeded coach for simulation
            val coach = _registeredUsers.value.find { it.role == UserRole.COACH }
            if (coach != null) {
                _currentUser.value = coach
                return
            }
        }
        
        val pkgId = packageId ?: when(role) {
            UserRole.STUDENT -> PackageCatalog.STUDENT_STANDART.id
            UserRole.COACH -> PackageCatalog.COACH_START.id
            UserRole.ADMIN -> "admin_pkg"
        }
        val name = when(role) {
            UserRole.STUDENT -> "Demo Öğrenci"
            UserRole.COACH -> "Demo Koç"
            UserRole.ADMIN -> "Demo Yönetici"
        }
        _currentUser.value = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = "demo@mahfaza.co",
            role = role,
            educationLevel = level,
            packageId = pkgId
        )
        if (role == UserRole.STUDENT) seedStudentData()
    }

    fun register(name: String, email: String, role: UserRole, level: EducationLevel?, packageId: String): Boolean {
        if (_registeredUsers.value.any { it.email.equals(email, ignoreCase = true) }) return false
        
        val pkg = PackageCatalog.getPackageById(packageId)
        val newUser = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            role = role,
            educationLevel = level,
            packageId = packageId,
            studentCapacity = pkg?.studentCapacity ?: 0
        )
        
        _registeredUsers.update { it + newUser }
        _currentUser.value = newUser
        if (role == UserRole.STUDENT) seedStudentData()
        return true
    }

    fun logout() {
        _currentUser.value = null
        _tasks.value = emptyList()
        _exams.value = emptyList()
    }

    fun hasEntitlement(entitlement: Entitlement): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role == UserRole.ADMIN) return true
        val pkg = PackageCatalog.getPackageById(user.packageId) ?: return false
        return pkg.entitlements.contains(entitlement)
    }

    fun toggleTask(taskId: String) {
        if (!hasEntitlement(Entitlement.DAILY_TASKS)) return
        _tasks.update { list ->
            list.map { task ->
                if (task.id == taskId) {
                    val newState = !task.isCompleted
                    if (newState) {
                        addXP(task.xpReward, "Görev Tamamlandı")
                    } else {
                        addXP(-task.xpReward, "Görev Geri Alındı")
                    }
                    task.copy(isCompleted = newState)
                } else task
            }
        }
    }

    private fun addXP(amount: Int, reason: String) {
        _currentUser.update { user ->
            user?.let {
                val newCurrentXp = (it.currentXp + amount).coerceAtLeast(0)
                // Lifetime XP only increases
                val newLifetimeXp = if (amount > 0) it.lifetimeXp + amount else it.lifetimeXp
                it.copy(currentXp = newCurrentXp, lifetimeXp = newLifetimeXp)
            }
        }
    }

    private fun seedStudentData() {
        _tasks.value = listOf(
            Task(UUID.randomUUID().toString(), "Matematik Soruları", "Matematik", "Üslü Sayılar", 50, 0, false, TaskPriority.HIGH, 20, 1),
            Task(UUID.randomUUID().toString(), "Türkçe Paragraf", "Türkçe", "Paragraf", 30, 0, false, TaskPriority.MEDIUM, 15, 1)
        )
        _exams.value = emptyList()
    }

    fun canAddStudent(): Boolean {
        val user = _currentUser.value ?: return false
        if (user.role != UserRole.COACH) return false
        return user.assignedStudents.size < user.studentCapacity
    }
    
    fun assignStudentToCoach(coachId: String, studentId: String) {
        _registeredUsers.update { users ->
            users.map { user ->
                if (user.id == coachId && user.role == UserRole.COACH) {
                    if (user.assignedStudents.size < user.studentCapacity && !user.assignedStudents.contains(studentId)) {
                        user.copy(assignedStudents = user.assignedStudents + studentId)
                    } else user
                } else user
            }
        }
    }

    fun updateUser(updatedUser: User) {
        _registeredUsers.update { users ->
            users.map { if (it.id == updatedUser.id) updatedUser else it }
        }
        if (_currentUser.value?.id == updatedUser.id) {
            _currentUser.value = updatedUser
        }
    }

    val allStudents: List<User> get() = _registeredUsers.value.filter { it.role == UserRole.STUDENT }
    val allCoaches: List<User> get() = _registeredUsers.value.filter { it.role == UserRole.COACH }
}

