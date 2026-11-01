package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MahfazaRepository
import com.example.data.CouponValidationResult
import com.example.data.MockRepository
import com.example.data.local.CouponEntity
import com.example.data.local.ExpenseEntity
import com.example.data.local.IncomeEntity
import com.example.data.local.PackageConfigEntity
import com.example.data.local.ReminderSettingsEntity
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MahfazaRepository(application)
    
    val currentUser = repository.currentUser
    
    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _exams = MutableStateFlow<List<ExamResult>>(emptyList())
    val exams: StateFlow<List<ExamResult>> = _exams.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()

    private val _reminderSettings = MutableStateFlow<ReminderSettingsEntity?>(null)
    val reminderSettings: StateFlow<ReminderSettingsEntity?> = _reminderSettings.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedDemoData()
            
            currentUser.collectLatest { user ->
                if (user != null) {
                    launch { repository.getTasks(user.id).collectLatest { _tasks.value = it } }
                    launch { repository.getExams(user.id).collectLatest { _exams.value = it } }
                    launch { repository.getNotificationsForUser(user.id).collectLatest { _notifications.value = it } }
                    launch { repository.getUnreadNotificationCount(user.id).collectLatest { _unreadNotificationCount.value = it } }
                    launch { repository.getReminderSettings(user.id).collectLatest { _reminderSettings.value = it } }
                    launch { com.example.util.ReminderManager.rescheduleAll(application) }
                } else {
                    _tasks.value = emptyList()
                    _exams.value = emptyList()
                    _notifications.value = emptyList()
                    _unreadNotificationCount.value = 0
                    _reminderSettings.value = null
                }
            }
        }
    }

    fun updateReminderSettings(settings: ReminderSettingsEntity) {
        viewModelScope.launch {
            repository.updateReminderSettings(settings)
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            repository.sendTestNotification()
        }
    }

    fun resetReminderSettings() {
        viewModelScope.launch {
            currentUser.value?.id?.let { repository.resetReminderSettings(it) }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            currentUser.value?.id?.let { repository.markAllNotificationsAsRead(it) }
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    val allStudents = repository.getAllStudents().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allCoaches = repository.getAllCoaches().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun handleGoogleAuthSuccess(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val fbUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (fbUser != null) {
                val email = fbUser.email ?: ""
                val name = fbUser.displayName ?: (if (repository.isUserAdmin(email)) "Serkan Koçak (Yönetici)" else "Kullanıcı")
                val isAdmin = repository.isUserAdmin(email)
                val user = User(
                    id = fbUser.uid,
                    name = name,
                    email = email,
                    role = if (isAdmin) UserRole.ADMIN else UserRole.STUDENT,
                    packageId = if (isAdmin) PackageCatalog.COACH_PRO.id else PackageCatalog.STUDENT_PRO.id,
                    currentXp = if (isAdmin) 50000 else 100,
                    lifetimeXp = if (isAdmin) 50000 else 100,
                    streak = 1
                )
                repository.saveFirebaseUser(user)
                onComplete()
            }
        }
    }

    fun login(email: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val cleanEmail = email.trim()
            if (repository.isUserAdmin(cleanEmail)) {
                repository.simulateLogin(UserRole.ADMIN)
                onResult(true)
                return@launch
            }
            var success = repository.login(cleanEmail)
            if (!success) {
                val user = when {
                    cleanEmail.contains("koc", ignoreCase = true) || cleanEmail.contains("coach", ignoreCase = true) -> {
                        repository.simulateLogin(UserRole.COACH)
                    }
                    cleanEmail.contains("admin", ignoreCase = true) -> {
                        repository.simulateLogin(UserRole.ADMIN)
                    }
                    cleanEmail.contains("lgs", ignoreCase = true) -> {
                        repository.simulateLogin(UserRole.STUDENT, EducationLevel.LGS_8TH)
                    }
                    cleanEmail.contains("yks", ignoreCase = true) || cleanEmail.contains("ogrenci", ignoreCase = true) || cleanEmail.contains("student", ignoreCase = true) -> {
                        repository.simulateLogin(UserRole.STUDENT, EducationLevel.YKS_TYT_AYT)
                    }
                    else -> null
                }
                success = user != null
            }
            onResult(success)
        }
    }
    
    fun getStudentTasks(studentId: String): Flow<List<Task>> = repository.getTasks(studentId)
    fun getStudentExams(studentId: String): Flow<List<ExamResult>> = repository.getExams(studentId)
    fun getStudentXPTransactions(studentId: String): Flow<List<XPTransaction>> = repository.getXPTransactions(studentId)
    suspend fun getStudentById(studentId: String): User? = repository.getUserById(studentId)

    // Reward Flow
    val rewards = repository.getAllRewards().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rewardRequests = repository.getRewardRequests().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun redeemReward(rewardId: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.redeemReward(user.id, rewardId)
            }
        }
    }

    fun updateRewardRequestStatus(requestId: String, status: RewardRequestStatus) {
        viewModelScope.launch {
            repository.updateRewardRequestStatus(requestId, status)
        }
    }

    val pendingApprovalTasks = repository.getPendingApprovalTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val approvedTasks = repository.getApprovedTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val rejectedTasks = repository.getRejectedTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allTasks = repository.getAllTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun approveTask(taskId: String) {
        viewModelScope.launch {
            repository.approveTask(taskId)
        }
    }

    fun archiveApprovedTask(taskId: String) {
        viewModelScope.launch {
            repository.archiveApprovedTask(taskId)
        }
    }

    fun archiveAllApprovedTasks() {
        viewModelScope.launch {
            repository.archiveAllApprovedTasks()
        }
    }

    fun rejectTask(taskId: String) {
        viewModelScope.launch {
            repository.rejectTask(taskId)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUserCompletely(userId)
        }
    }

    fun updateTask(studentId: String, task: Task) {
        viewModelScope.launch {
            repository.updateTask(studentId, task)
        }
    }

    fun addReward(name: String, description: String, xpCost: Int, stock: Int) {
        if (xpCost < 0 || stock < 0) return
        viewModelScope.launch {
            repository.addReward(
                Reward(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    xpCost = xpCost,
                    stock = stock
                )
            )
        }
    }

    fun updateReward(reward: Reward) {
        if (reward.xpCost < 0 || reward.stock < 0) return
        viewModelScope.launch {
            repository.updateReward(reward)
        }
    }

    fun deleteReward(rewardId: String) {
        viewModelScope.launch {
            repository.deleteReward(rewardId)
        }
    }

    fun adjustXP(studentId: String, amount: Int, reason: String) {
        viewModelScope.launch {
            repository.adjustXP(studentId, amount, reason)
        }
    }

    fun addTask(studentId: String, task: Task) {
        viewModelScope.launch {
            repository.addTask(studentId, task)
        }
    }

    fun uploadTaskProof(taskId: String, photoUri: String) {
        viewModelScope.launch {
            repository.updateTaskWithProof(taskId, photoUri)
        }
    }

    // For demo role switching
    fun simulateLogin(role: UserRole, level: EducationLevel? = null) {
        viewModelScope.launch {
            repository.simulateLogin(role, level)
        }
    }

    suspend fun register(
        name: String,
        email: String,
        role: UserRole,
        level: EducationLevel?,
        packageId: String,
        examTracks: List<String> = emptyList(),
        subscriptionPlan: String = "FREE",
        coachCodeInput: String? = null
    ): Boolean {
        return repository.register(name, email, role, level, packageId, examTracks, subscriptionPlan, coachCodeInput)
    }

    fun updateUserSubscriptionPlan(packageId: String, subscriptionPlan: String) {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch
            repository.updateUserSubscriptionPlan(user.id, packageId, subscriptionPlan)
        }
    }

    fun logout() {
        repository.logout()
    }

    fun toggleTask(taskId: String) {
        viewModelScope.launch {
            currentUser.value?.let { user ->
                repository.toggleTask(user.id, taskId)
            }
        }
    }

    fun hasEntitlement(entitlement: Entitlement): Boolean {
        val user = currentUser.value ?: return false
        if (user.role == UserRole.ADMIN) return true
        val pkg = PackageCatalog.getPackageById(user.packageId) ?: return false
        return pkg.entitlements.contains(entitlement)
    }

    fun canAddStudent(): Boolean {
        val user = currentUser.value ?: return false
        if (user.role != UserRole.COACH) return false
        return user.assignedStudents.size < user.studentCapacity
    }

    val allMeetings = repository.getAllMeetings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getStudentMeetings(studentId: String): Flow<List<ParentMeeting>> = repository.getMeetings(studentId)

    fun addMeeting(meeting: ParentMeeting) {
        viewModelScope.launch {
            repository.addMeeting(meeting)
        }
    }

    fun updateMeeting(meeting: ParentMeeting) {
        viewModelScope.launch {
            repository.updateMeeting(meeting)
        }
    }

    fun deleteMeeting(meeting: ParentMeeting) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
        }
    }

    fun updateUserAvatar(userId: String, avatarUri: String) {
        viewModelScope.launch {
            repository.updateUserAvatar(userId, avatarUri)
        }
    }

    fun addExam(userId: String, exam: ExamResult) {
        viewModelScope.launch {
            repository.addExam(userId, exam)
        }
    }

    fun createAndAssignStudent(
        name: String,
        email: String,
        level: EducationLevel,
        packageId: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val coachId = currentUser.value?.id ?: return@launch
            val success = repository.createAndAssignStudent(coachId, name, email, level, packageId)
            onResult(success)
        }
    }

    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            repository.updateUser(updatedUser)
        }
    }

    fun getOrGenerateCoachCode(coachId: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val code = repository.getOrGenerateCoachCode(coachId)
            onResult(code)
        }
    }

    fun linkStudentToCoachByCode(code: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val studentId = currentUser.value?.id ?: return@launch
            val success = repository.linkStudentToCoachByCode(studentId, code)
            onResult(success)
        }
    }

    fun savePomodoroSession(durationMinutes: Int, mode: String, subject: String = "Genel Odaklanma") {
        viewModelScope.launch {
            val studentId = currentUser.value?.id ?: return@launch
            repository.savePomodoroSession(studentId, durationMinutes, mode, subject)
        }
    }

    fun getPomodoroSessions(studentId: String): Flow<List<PomodoroSession>> = repository.getPomodoroSessions(studentId)

    fun updateStudentAcademicProfile(
        targetUniversity: String,
        targetDepartment: String,
        targetRanking: String,
        goals: String
    ) {
        viewModelScope.launch {
            val studentId = currentUser.value?.id ?: return@launch
            repository.updateStudentAcademicProfile(studentId, targetUniversity, targetDepartment, targetRanking, goals)
        }
    }

    fun getChatMessages(otherId: String): Flow<List<ChatMessage>> {
        val myId = currentUser.value?.id ?: return flowOf(emptyList())
        return repository.getChatMessages(myId, otherId)
    }

    fun sendMessage(receiverId: String, content: String) {
        val myId = currentUser.value?.id ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(myId, receiverId, content.trim())
        }
    }

    val allExpenses = repository.getAllExpenses().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.addExpense(expense) }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch { repository.deleteExpense(expenseId) }
    }

    // Income
    val allIncomes = repository.getAllIncomes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val totalIncome = repository.getTotalIncomeFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addIncome(income: IncomeEntity) {
        viewModelScope.launch { repository.addIncome(income) }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch { repository.deleteIncome(income) }
    }

    // Package Config
    val allPackageConfigs = repository.getAllPackageConfigs().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updatePackageConfig(config: PackageConfigEntity) {
        viewModelScope.launch { repository.updatePackageConfig(config) }
    }

    fun addPackageConfig(config: PackageConfigEntity) {
        viewModelScope.launch { repository.addPackageConfig(config) }
    }

    // Coupon Flow & Management
    val allCoupons = repository.getAllCoupons().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun validateCoupon(
        code: String,
        packageId: String,
        billingPeriod: String,
        originalPrice: Double
    ): CouponValidationResult {
        val userId = currentUser.value?.id ?: ""
        return repository.validateCoupon(code, userId, packageId, billingPeriod, originalPrice)
    }

    suspend fun processTestPayment(
        packageId: String,
        billingPeriod: String,
        appliedCouponCode: String?,
        originalPrice: Double
    ): Boolean {
        val userId = currentUser.value?.id ?: return false
        return repository.processTestPayment(userId, packageId, billingPeriod, appliedCouponCode, originalPrice)
    }

    fun addCoupon(coupon: CouponEntity) {
        viewModelScope.launch { repository.addCoupon(coupon) }
    }

    fun updateCoupon(coupon: CouponEntity) {
        viewModelScope.launch { repository.updateCoupon(coupon) }
    }

    fun deleteCoupon(code: String) {
        viewModelScope.launch { repository.deleteCoupon(code) }
    }

    fun toggleCouponActiveStatus(code: String) {
        viewModelScope.launch { repository.toggleCouponActiveStatus(code) }
    }

    // Backup & Restore
    suspend fun getBackupData(): String = repository.getBackupData()
    suspend fun restoreDatabaseFromJson(json: String): Boolean = repository.restoreDatabaseFromJson(json)
}

