package com.example.data

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.*
import com.example.model.*
import com.example.util.LevelCalculator
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class MahfazaRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val taskDao = db.taskDao()
    private val examDao = db.examDao()
    private val messageDao = db.messageDao()
    private val xpDao = db.xpDao()
    private val rewardDao = db.rewardDao()
    private val rewardRequestDao = db.rewardRequestDao()
    private val notificationDao = db.notificationDao()

    val firestoreRepository: com.example.data.firestore.FirestoreRepository by lazy {
        com.example.data.firestore.FirestoreRepository(context)
    }

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun isUserAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim().lowercase()
        return clean == "serkankocak551@gmail.com"
    }

    suspend fun saveFirebaseUser(user: User) {
        val finalUser = if (isUserAdmin(user.email)) {
            user.copy(
                role = UserRole.ADMIN,
                name = if (user.name.isBlank()) "Serkan Koçak (Yönetici)" else user.name,
                packageId = PackageCatalog.COACH_PRO.id,
                currentXp = 50000,
                lifetimeXp = 50000,
                streak = 30
            )
        } else {
            user
        }
        userDao.insertUser(finalUser.toEntity())
        _currentUser.value = finalUser
        try {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null) {
                firestoreRepository.saveUser(finalUser)
            }
        } catch (_: Exception) {}
    }

    suspend fun login(email: String): Boolean {
        val cleanEmail = email.trim().lowercase()
        if (isUserAdmin(cleanEmail)) {
            var adminUser = userDao.getUserByEmail(cleanEmail)
            if (adminUser == null) {
                val newAdmin = User(
                    id = "admin_serkankocak551",
                    name = "Serkan Koçak (Yönetici)",
                    email = "serkankocak551@gmail.com",
                    role = UserRole.ADMIN,
                    packageId = PackageCatalog.COACH_PRO.id,
                    currentXp = 50000,
                    lifetimeXp = 50000,
                    streak = 30
                )
                userDao.insertUser(newAdmin.toEntity())
                adminUser = newAdmin.toEntity()
            }
            _currentUser.value = adminUser.toModel()
            return true
        }

        val userEntity = userDao.getUserByEmail(email)
        return if (userEntity != null) {
            _currentUser.value = userEntity.toModel()
            true
        } else {
            false
        }
    }

    suspend fun simulateLogin(role: UserRole, level: EducationLevel? = null): User {
        seedDemoData()
        val email = when (role) {
            UserRole.STUDENT -> if (level == EducationLevel.LGS_8TH) "lgs@mahfaza.co" else "yks@mahfaza.co"
            UserRole.COACH -> "demokoc@mahfaza.co"
            UserRole.ADMIN -> "serkankocak551@gmail.com"
        }

        var userEntity = userDao.getUserByEmail(email)
        if (userEntity == null) {
            val newUser = when (role) {
                UserRole.STUDENT -> User(
                    id = if (level == EducationLevel.LGS_8TH) "s2" else "s1",
                    name = if (level == EducationLevel.LGS_8TH) "Demo Öğrenci LGS" else "Demo Öğrenci YKS",
                    email = email,
                    role = UserRole.STUDENT,
                    educationLevel = level ?: EducationLevel.YKS_TYT_AYT,
                    packageId = PackageCatalog.STUDENT_PRO.id,
                    currentXp = 1250,
                    lifetimeXp = 2500,
                    streak = 12,
                    assignedCoachId = "c1"
                )
                UserRole.COACH -> User(
                    id = "c1",
                    name = "Demo Koç",
                    email = email,
                    role = UserRole.COACH,
                    packageId = PackageCatalog.COACH_PRO.id,
                    studentCapacity = 25,
                    assignedStudents = listOf("s1", "s2"),
                    title = "Eğitim Koçu & Akademik Danışman",
                    coachCode = "KOC-DEMO1"
                )
                UserRole.ADMIN -> User(
                    id = "admin_serkankocak551",
                    name = "Serkan Koçak (Yönetici)",
                    email = email,
                    role = UserRole.ADMIN,
                    packageId = PackageCatalog.COACH_PRO.id,
                    currentXp = 50000,
                    lifetimeXp = 50000,
                    streak = 30
                )
            }
            userDao.insertUser(newUser.toEntity())
            userEntity = newUser.toEntity()
        }

        val user = userEntity.toModel()
        _currentUser.value = user
        return user
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
        if (userDao.getUserByEmail(email) != null) return false
        
        val pkg = PackageCatalog.getPackageById(packageId)
        val generatedCoachCode = if (role == UserRole.COACH) "KOC-" + UUID.randomUUID().toString().take(6).uppercase() else null
        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            role = role,
            educationLevel = level,
            packageId = packageId,
            studentCapacity = pkg?.studentCapacity ?: 0,
            coachCode = generatedCoachCode,
            examTracks = examTracks,
            subscriptionPlan = subscriptionPlan
        )
        
        userDao.insertUser(user.toEntity())
        _currentUser.value = user

        if (role == UserRole.STUDENT && !coachCodeInput.isNullOrBlank()) {
            linkStudentToCoachByCode(user.id, coachCodeInput.trim())
        }

        return true
    }

    suspend fun updateUserSubscriptionPlan(userId: String, packageId: String, subscriptionPlan: String): Boolean {
        val userEntity = userDao.getUserById(userId) ?: return false
        val pkg = PackageCatalog.getPackageById(packageId)
        val updated = userEntity.copy(
            packageId = packageId,
            subscriptionPlan = subscriptionPlan,
            studentCapacity = if (userEntity.role == UserRole.COACH) pkg?.studentCapacity ?: userEntity.studentCapacity else userEntity.studentCapacity
        )
        userDao.updateUser(updated)
        if (_currentUser.value?.id == userId) {
            _currentUser.value = updated.toModel()
        }
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    // --- NOTIFICATIONS ENGINE ---
    suspend fun sendNotification(
        recipientId: String,
        type: NotificationType,
        title: String,
        message: String,
        relatedEntityId: String? = null,
        navigationRoute: String? = null,
        idempotencyKey: String
    ) {
        val notification = NotificationEntity(
            id = UUID.randomUUID().toString(),
            recipientId = recipientId,
            type = type,
            title = title,
            message = message,
            relatedEntityId = relatedEntityId,
            timestamp = System.currentTimeMillis(),
            isRead = false,
            navigationRoute = navigationRoute,
            idempotencyKey = idempotencyKey
        )
        val result = notificationDao.insertNotification(notification)
        if (result != -1L) {
            NotificationHelper.showSystemNotification(context, title, message)
        }
    }

    fun getNotificationsForUser(userId: String): Flow<List<AppNotification>> {
        return notificationDao.getNotificationsForUser(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getUnreadNotificationCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        notificationDao.markAllAsRead(userId)
    }

    suspend fun markNotificationAsRead(id: String) {
        notificationDao.markAsRead(id)
    }

    // --- REMINDER SETTINGS ---
    fun getReminderSettings(userId: String): Flow<ReminderSettingsEntity?> {
        return db.reminderSettingsDao().getReminderSettings(userId)
    }

    suspend fun updateReminderSettings(settings: ReminderSettingsEntity) {
        db.reminderSettingsDao().insertReminderSettings(settings)
        com.example.util.ReminderManager.rescheduleAll(context)
    }

    suspend fun sendTestNotification() {
        com.example.util.NotificationHelper.showSystemNotification(
            context,
            "Mahfaza: Test Bildirimi",
            "Hatırlatma sisteminiz başarıyla çalışıyor! 🎯",
            isReminder = true
        )
    }

    suspend fun resetReminderSettings(userId: String) {
        val defaultSettings = ReminderSettingsEntity(userId = userId)
        updateReminderSettings(defaultSettings)
    }

    // --- CENTRALIZED XP ENGINE WITH DAILY CAP & IDEMPOTENCY ---
    suspend fun awardXPWithRules(
        studentId: String,
        proposedXp: Int,
        reason: String,
        sourceRef: String? = null
    ): Int {
        if (proposedXp <= 0) return 0

        return db.withTransaction {
            // 1. Idempotency Check
            if (!sourceRef.isNullOrBlank()) {
                val existing = xpDao.getTransactionBySource(studentId, sourceRef)
                if (existing != null) return@withTransaction 0
            }

            // 2. Daily Cap Check (100 XP max per local day in Europe/Istanbul)
            val tz = TimeZone.getTimeZone("Europe/Istanbul")
            val calendar = Calendar.getInstance(tz)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 86_400_000L

            val earnedToday = xpDao.getEarnedXpToday(studentId, startOfDay, endOfDay) ?: 0
            val remainingCap = (com.example.util.XpPolicy.DAILY_MAX_XP - earnedToday).coerceAtLeast(0)
            val actualAward = minOf(proposedXp, remainingCap)

            if (actualAward <= 0) return@withTransaction 0

            val userEntity = userDao.getUserById(studentId) ?: return@withTransaction 0
            val user = userEntity.toModel()

            val oldLevelInfo = LevelCalculator.calculateLevel(user.lifetimeXp)
            val newCurrentXp = user.currentXp + actualAward
            val newLifetimeXp = user.lifetimeXp + actualAward
            val newLevelInfo = LevelCalculator.calculateLevel(newLifetimeXp)

            val updatedUser = user.copy(currentXp = newCurrentXp, lifetimeXp = newLifetimeXp)
            userDao.updateUser(updatedUser.toEntity())

            if (_currentUser.value?.id == studentId) {
                _currentUser.value = updatedUser
            }

            xpDao.insertXPTransaction(
                XPTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = studentId,
                    amount = actualAward,
                    type = XPTransactionType.EARNED,
                    reason = reason,
                    timestamp = System.currentTimeMillis(),
                    sourceReference = sourceRef
                )
            )

            // 3. Check Level Up
            if (newLevelInfo.levelNumber > oldLevelInfo.levelNumber) {
                sendNotification(
                    recipientId = studentId,
                    type = NotificationType.LEVEL_UP,
                    title = "Tebrikler! Seviye ${newLevelInfo.levelNumber}'e Ulaştın! 🎉",
                    message = "Yeni seviye unvanın: ${newLevelInfo.title}. Harika ilerliyorsun!",
                    relatedEntityId = newLevelInfo.levelNumber.toString(),
                    idempotencyKey = "LEVEL_UP:${studentId}:${newLevelInfo.levelNumber}"
                )
            }

            actualAward
        }
    }

    suspend fun adjustXP(studentId: String, amount: Int, reason: String) {
        val userEntity = userDao.getUserById(studentId) ?: return
        val user = userEntity.toModel()

        val newCurrentXp = (user.currentXp + amount).coerceAtLeast(0)
        val newLifetimeXp = if (amount > 0) user.lifetimeXp + amount else user.lifetimeXp

        val updatedUser = user.copy(currentXp = newCurrentXp, lifetimeXp = newLifetimeXp)
        userDao.updateUser(updatedUser.toEntity())

        if (_currentUser.value?.id == studentId) {
            _currentUser.value = updatedUser
        }

        xpDao.insertXPTransaction(
            XPTransactionEntity(
                id = UUID.randomUUID().toString(),
                userId = studentId,
                amount = amount,
                type = if (amount >= 0) XPTransactionType.EARNED else XPTransactionType.SPENT,
                reason = reason,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    // --- TASKS & STUDY SUBMISSIONS ---
    fun getTasks(userId: String): Flow<List<Task>> {
        return taskDao.getTasksByUserId(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getPendingApprovalTasks(): Flow<List<Pair<User, Task>>> {
        return combine(
            taskDao.getPendingApprovalTasks(),
            userDao.getAllStudents()
        ) { taskEntities, studentEntities ->
            val studentMap = studentEntities.associateBy { it.id }
            taskEntities.mapNotNull { taskEntity ->
                val student = studentMap[taskEntity.userId]?.toModel()
                if (student != null) student to taskEntity.toModel() else null
            }
        }
    }

    fun getApprovedTasks(): Flow<List<Pair<User, Task>>> {
        return combine(
            taskDao.getApprovedTasks(),
            userDao.getAllStudents()
        ) { taskEntities, studentEntities ->
            val studentMap = studentEntities.associateBy { it.id }
            taskEntities.mapNotNull { taskEntity ->
                val student = studentMap[taskEntity.userId]?.toModel()
                if (student != null) student to taskEntity.toModel() else null
            }
        }
    }

    fun getRejectedTasks(): Flow<List<Pair<User, Task>>> {
        return combine(
            taskDao.getRejectedTasks(),
            userDao.getAllStudents()
        ) { taskEntities, studentEntities ->
            val studentMap = studentEntities.associateBy { it.id }
            taskEntities.mapNotNull { taskEntity ->
                val student = studentMap[taskEntity.userId]?.toModel()
                if (student != null) student to taskEntity.toModel() else null
            }
        }
    }

    suspend fun addTask(userId: String, task: Task) {
        taskDao.insertTask(task.toEntity(userId))
        val studentEntity = userDao.getUserById(userId)
        if (studentEntity != null) {
            val student = studentEntity.toModel()
            if (task.photoProofUri != null && student.assignedCoachId != null) {
                // Student study submission -> Notify coach
                sendNotification(
                    recipientId = student.assignedCoachId,
                    type = NotificationType.STUDY_SUBMITTED,
                    title = "Yeni Çalışma Onayı Bekliyor 📝",
                    message = "${student.name} '${task.title}' çalışma kaydını ve kanıtını gönderdi.",
                    relatedEntityId = task.id,
                    idempotencyKey = "STUDY_SUBMITTED:${task.id}"
                )
            } else if (student.assignedCoachId == _currentUser.value?.id) {
                // Coach task assignment -> Notify student
                sendNotification(
                    recipientId = userId,
                    type = NotificationType.TASK_ASSIGNED,
                    title = "Yeni Görev Atandı 🎯",
                    message = "Koçun sana yeni bir görev atadı: ${task.title}",
                    relatedEntityId = task.id,
                    idempotencyKey = "TASK_ASSIGNED:${task.id}"
                )
            }
        }

        // Task Reminder Scheduling
        val settings = db.reminderSettingsDao().getReminderSettingsSync(userId)
        if (settings?.notificationsEnabled == true && settings.taskRemindersEnabled) {
            com.example.util.ReminderManager.scheduleTaskReminder(context, task)
        }
    }

    suspend fun updateTask(userId: String, task: Task) {
        taskDao.updateTask(task.toEntity(userId))
        
        val settings = db.reminderSettingsDao().getReminderSettingsSync(userId)
        if (settings?.notificationsEnabled == true && settings.taskRemindersEnabled) {
            if (task.isCompleted) {
                com.example.util.ReminderManager.cancelReminder(context, 2000 + task.id.hashCode())
            } else {
                com.example.util.ReminderManager.scheduleTaskReminder(context, task)
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        com.example.util.ReminderManager.cancelReminder(context, 2000 + taskId.hashCode())
        taskDao.deleteTaskById(taskId)
    }

    suspend fun updateTaskWithProof(taskId: String, photoUri: String) {
        val taskEntity = taskDao.getTaskById(taskId) ?: return
        val updatedTask = taskEntity.copy(photoProofUri = photoUri, isCompleted = true, isRejected = false)
        taskDao.updateTask(updatedTask)
    }

    suspend fun approveTask(taskId: String) {
        val taskEntity = taskDao.getTaskById(taskId) ?: return
        if (taskEntity.isApproved) return

        val updatedTask = taskEntity.copy(isApproved = true, isCompleted = true, isRejected = false)
        taskDao.updateTask(updatedTask)

        val qCount = taskEntity.targetQuestionCount
        val proposedXp = if (taskEntity.photoProofUri != null) {
            com.example.util.XpPolicy.calculateStudyXp(qCount)
        } else {
            com.example.util.XpPolicy.calculateTaskXp(taskEntity.xpReward)
        }

        val awardedXp = awardXPWithRules(
            studentId = taskEntity.userId,
            proposedXp = proposedXp,
            reason = "Çalışma Onayı: ${taskEntity.title}",
            sourceRef = "STUDY_APPROVAL:$taskId"
        )

        sendNotification(
            recipientId = taskEntity.userId,
            type = NotificationType.STUDY_APPROVED,
            title = "Çalışman Onaylandı! ✨",
            message = "'${taskEntity.title}' çalışman koçun tarafından onaylandı (+$awardedXp XP).",
            relatedEntityId = taskId,
            idempotencyKey = "STUDY_APPROVED:$taskId"
        )
    }

    suspend fun archiveApprovedTask(taskId: String) {
        taskDao.archiveTask(taskId)
    }

    suspend fun archiveAllApprovedTasks() {
        taskDao.archiveAllApprovedTasks()
    }

    suspend fun rejectTask(taskId: String) {
        val taskEntity = taskDao.getTaskById(taskId) ?: return
        if (taskEntity.isRejected) return

        val updatedTask = taskEntity.copy(isRejected = true, isApproved = false)
        taskDao.updateTask(updatedTask)

        sendNotification(
            recipientId = taskEntity.userId,
            type = NotificationType.STUDY_REJECTED,
            title = "Çalışma Düzeltme Bekliyor ⚠️",
            message = "'${taskEntity.title}' çalışman koçun tarafından düzeltme için geri gönderildi.",
            relatedEntityId = taskId,
            idempotencyKey = "STUDY_REJECTED:$taskId"
        )
    }

    suspend fun toggleTask(userId: String, taskId: String) {
        val taskEntity = taskDao.getTaskById(taskId) ?: return
        val newState = !taskEntity.isCompleted
        
        val updatedTask = taskEntity.copy(isCompleted = newState)
        taskDao.updateTask(updatedTask)
    }

    fun getXPTransactions(userId: String): Flow<List<XPTransaction>> {
        return xpDao.getXPTransactionsByUserId(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    // --- REWARDS ---
    fun getAllRewards(): Flow<List<Reward>> {
        return rewardDao.getAllRewards().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun addReward(reward: Reward) {
        if (reward.xpCost < 0 || reward.stock < 0) return
        rewardDao.insertReward(reward.toEntity())
    }

    suspend fun updateReward(reward: Reward) {
        if (reward.xpCost < 0 || reward.stock < 0) return
        rewardDao.updateReward(reward.toEntity())
    }

    suspend fun deleteReward(rewardId: String) {
        rewardDao.deleteRewardById(rewardId)
    }

    suspend fun redeemReward(studentId: String, rewardId: String): Boolean {
        val userEntity = userDao.getUserById(studentId) ?: return false
        val user = userEntity.toModel()
        val rewardEntity = rewardDao.getRewardById(rewardId) ?: return false
        val reward = rewardEntity.toModel()

        if (user.currentXp < reward.xpCost || reward.stock <= 0) return false

        val reqId = UUID.randomUUID().toString()
        rewardRequestDao.insertRewardRequest(
            RewardRequestEntity(
                id = reqId,
                studentId = studentId,
                studentName = user.name,
                rewardId = rewardId,
                rewardName = reward.name,
                xpCost = reward.xpCost,
                timestamp = System.currentTimeMillis(),
                status = RewardRequestStatus.PENDING
            )
        )

        if (user.assignedCoachId != null) {
            sendNotification(
                recipientId = user.assignedCoachId,
                type = NotificationType.REWARD_REQUESTED,
                title = "Yeni Ödül Talebi 🎁",
                message = "${user.name} '${reward.name}' ödülünü talep etti.",
                relatedEntityId = reqId,
                idempotencyKey = "REWARD_REQ:$reqId"
            )
        }
        return true
    }

    fun getRewardRequests(): Flow<List<RewardRequest>> {
        return rewardRequestDao.getAllRewardRequests().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun approveRewardRequest(requestId: String): Boolean {
        return db.withTransaction {
            val request = rewardRequestDao.getRewardRequestById(requestId) ?: return@withTransaction false
            if (request.status != RewardRequestStatus.PENDING) return@withTransaction false

            val userEntity = userDao.getUserById(request.studentId) ?: return@withTransaction false
            val rewardEntity = rewardDao.getRewardById(request.rewardId) ?: return@withTransaction false

            if (userEntity.currentXp < request.xpCost || rewardEntity.stock <= 0) return@withTransaction false

            val updatedUser = userEntity.copy(
                currentXp = (userEntity.currentXp - request.xpCost).coerceAtLeast(0)
            )
            userDao.updateUser(updatedUser)

            if (_currentUser.value?.id == request.studentId) {
                _currentUser.value = updatedUser.toModel()
            }

            rewardDao.updateReward(rewardEntity.copy(stock = rewardEntity.stock - 1))

            xpDao.insertXPTransaction(
                XPTransactionEntity(
                    id = UUID.randomUUID().toString(),
                    userId = request.studentId,
                    amount = -request.xpCost,
                    type = XPTransactionType.SPENT,
                    reason = "Ödül Onaylandı: ${request.rewardName}",
                    timestamp = System.currentTimeMillis(),
                    sourceReference = "REWARD_SPENT:$requestId"
                )
            )

            rewardRequestDao.updateRewardRequest(request.copy(status = RewardRequestStatus.APPROVED))

            sendNotification(
                recipientId = request.studentId,
                type = NotificationType.REWARD_STATUS,
                title = "Ödül Talebin Onaylandı! 🎉",
                message = "'${request.rewardName}' ödül talebin koçun tarafından onaylandı.",
                relatedEntityId = requestId,
                idempotencyKey = "REWARD_APP:$requestId"
            )

            true
        }
    }

    suspend fun rejectRewardRequest(requestId: String): Boolean {
        val request = rewardRequestDao.getRewardRequestById(requestId) ?: return false
        if (request.status != RewardRequestStatus.PENDING) return false

        rewardRequestDao.updateRewardRequest(request.copy(status = RewardRequestStatus.REJECTED))

        sendNotification(
            recipientId = request.studentId,
            type = NotificationType.REWARD_STATUS,
            title = "Ödül Talebin Reddedildi",
            message = "'${request.rewardName}' ödül talebin koçun tarafından reddedildi.",
            relatedEntityId = requestId,
            idempotencyKey = "REWARD_REJ:$requestId"
        )
        return true
    }

    suspend fun updateRewardRequestStatus(requestId: String, status: RewardRequestStatus) {
        when (status) {
            RewardRequestStatus.APPROVED -> approveRewardRequest(requestId)
            RewardRequestStatus.REJECTED -> rejectRewardRequest(requestId)
            else -> {}
        }
    }

    // --- COACH LINKING ---
    suspend fun getOrGenerateCoachCode(coachId: String): String {
        val coach = userDao.getUserById(coachId) ?: return ""
        if (!coach.coachCode.isNullOrBlank()) return coach.coachCode

        val newCode = "KOC-" + UUID.randomUUID().toString().take(6).uppercase()
        val updatedCoach = coach.copy(coachCode = newCode)
        userDao.updateUser(updatedCoach)
        if (_currentUser.value?.id == coachId) {
            _currentUser.value = updatedCoach.toModel()
        }
        return newCode
    }

    suspend fun linkStudentToCoachByCode(studentId: String, code: String): Boolean {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) return false

        val coachEntity = userDao.getUserByCoachCode(cleanCode) ?: return false
        if (coachEntity.role != UserRole.COACH) return false

        val studentEntity = userDao.getUserById(studentId) ?: return false

        val updatedStudent = studentEntity.copy(assignedCoachId = coachEntity.id)
        userDao.updateUser(updatedStudent)

        if (!coachEntity.assignedStudents.contains(studentId)) {
            val updatedAssigned = coachEntity.assignedStudents + studentId
            userDao.updateUser(coachEntity.copy(assignedStudents = updatedAssigned))
        }

        if (_currentUser.value?.id == studentId) {
            _currentUser.value = updatedStudent.toModel()
        }

        sendNotification(
            recipientId = coachEntity.id,
            type = NotificationType.SYSTEM_ANNOUNCEMENT,
            title = "Yeni Öğrenci Eşleşti 🤝",
            message = "${studentEntity.name} seninle koçluk kodun üzerinden eşleşti.",
            relatedEntityId = studentId,
            idempotencyKey = "LINK:${studentId}:${coachEntity.id}"
        )
        return true
    }

    // --- POMODORO ---
    suspend fun savePomodoroSession(studentId: String, durationMinutes: Int, mode: String, subject: String = "Genel Odaklanma") {
        val sessionId = UUID.randomUUID().toString()
        db.withTransaction {
            val session = PomodoroSessionEntity(
                id = sessionId,
                studentId = studentId,
                durationMinutes = durationMinutes,
                mode = mode,
                subject = subject,
                timestamp = System.currentTimeMillis()
            )
            db.pomodoroDao().insertPomodoroSession(session)

            val proposedXp = com.example.util.XpPolicy.calculatePomodoroXp(durationMinutes)
            if (proposedXp > 0) {
                awardXPWithRules(
                    studentId = studentId,
                    proposedXp = proposedXp,
                    reason = "Pomodoro Odaklanma Tamamlandı ($mode)",
                    sourceRef = "POMODORO:$sessionId"
                )
            }
        }
    }

    fun getPomodoroSessions(studentId: String): Flow<List<PomodoroSession>> {
        return db.pomodoroDao().getPomodoroSessions(studentId).map { list ->
            list.map { it.toModel() }
        }
    }

    // --- ACADEMIC PROFILE ---
    suspend fun updateStudentAcademicProfile(
        studentId: String,
        targetUniversity: String,
        targetDepartment: String,
        targetRanking: String,
        goals: String
    ) {
        val user = userDao.getUserById(studentId) ?: return
        val updated = user.copy(
            targetUniversity = targetUniversity,
            targetDepartment = targetDepartment,
            targetRanking = targetRanking,
            goals = goals
        )
        userDao.updateUser(updated)
        if (_currentUser.value?.id == studentId) {
            _currentUser.value = updated.toModel()
        }
    }

    // --- EXAMS ---
    fun getExams(userId: String): Flow<List<ExamResult>> {
        return examDao.getExamsByUserId(userId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun addExam(userId: String, exam: ExamResult) {
        db.withTransaction {
            examDao.insertExam(exam.toEntity(userId))
            awardXPWithRules(
                studentId = userId,
                proposedXp = com.example.util.XpPolicy.EXAM_XP,
                reason = "Deneme Sınavı Girildi: ${exam.name}",
                sourceRef = "EXAM_ENTRY:${exam.id}"
            )
        }
        val user = userDao.getUserById(userId)
        if (user?.assignedCoachId != null) {
            sendNotification(
                recipientId = user.assignedCoachId,
                type = NotificationType.EXAM_SAVED,
                title = "Yeni Deneme Sonucu 📊",
                message = "${user.name} '${exam.name}' deneme sonucunu kaydetti.",
                relatedEntityId = exam.id,
                idempotencyKey = "EXAM_NOTIF:${exam.id}"
            )
        }
    }

    fun getAllStudents(): Flow<List<User>> {
        return userDao.getAllStudents().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toModel()
    }

    suspend fun updateUser(user: User) {
        userDao.updateUser(user.toEntity())
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = user
        }
    }

    fun getAllCoaches(): Flow<List<User>> {
        return userDao.getAllCoaches().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getMeetings(studentId: String): Flow<List<ParentMeeting>> {
        return db.parentMeetingDao().getMeetingsByStudentId(studentId).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getAllMeetings(): Flow<List<ParentMeeting>> {
        return db.parentMeetingDao().getAllMeetings().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun addMeeting(meeting: ParentMeeting) {
        db.parentMeetingDao().insertMeeting(meeting.toEntity())
    }

    suspend fun updateMeeting(meeting: ParentMeeting) {
        db.parentMeetingDao().updateMeeting(meeting.toEntity())
    }

    suspend fun deleteMeeting(meeting: ParentMeeting) {
        db.parentMeetingDao().deleteMeeting(meeting.toEntity())
    }

    suspend fun updateUserAvatar(userId: String, avatarUri: String) {
        val user = userDao.getUserById(userId) ?: return
        val updated = user.copy(avatarUri = avatarUri)
        userDao.updateUser(updated)
        if (_currentUser.value?.id == userId) {
            _currentUser.value = updated.toModel()
        }
    }

    suspend fun createAndAssignStudent(
        coachId: String,
        name: String,
        email: String,
        level: EducationLevel,
        packageId: String
    ): Boolean {
        if (userDao.getUserByEmail(email) != null) return false

        val newStudentId = UUID.randomUUID().toString()
        val student = User(
            id = newStudentId,
            name = name,
            email = email,
            role = UserRole.STUDENT,
            educationLevel = level,
            packageId = packageId,
            assignedCoachId = coachId
        )
        userDao.insertUser(student.toEntity())

        val coach = userDao.getUserById(coachId)
        if (coach != null) {
            val updatedList = coach.assignedStudents + newStudentId
            userDao.updateUser(coach.copy(assignedStudents = updatedList))
            if (_currentUser.value?.id == coachId) {
                _currentUser.value = coach.copy(assignedStudents = updatedList).toModel()
            }
        }
        return true
    }

    fun getChatMessages(userId: String, otherId: String): Flow<List<ChatMessage>> {
        return messageDao.getChatMessages(userId, otherId).map { list ->
            list.map {
                ChatMessage(
                    id = it.id,
                    senderId = it.senderId,
                    receiverId = it.receiverId,
                    content = it.content,
                    timestamp = it.timestamp,
                    isRead = it.isRead
                )
            }
        }
    }

    suspend fun sendMessage(senderId: String, receiverId: String, content: String) {
        val entity = MessageEntity(
            id = UUID.randomUUID().toString(),
            senderId = senderId,
            receiverId = receiverId,
            content = content,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )
        messageDao.insertMessage(entity)
    }

    // --- SEEDING DATA ---
    suspend fun seedDemoData() {
        // Seed Coupons
        if (db.couponDao().getAllCouponsSync().isEmpty()) {
            val now = System.currentTimeMillis()
            val oneYearLater = now + (365L * 24 * 60 * 60 * 1000)
            val demoCoupons = listOf(
                CouponEntity(
                    code = "SERKAN20",
                    discountType = "PERCENTAGE",
                    discountValue = 20.0,
                    startDate = now - 86400000L,
                    endDate = oneYearLater,
                    totalUsageLimit = 500,
                    perUserLimit = 1,
                    usedCount = 0,
                    isActive = true
                ),
                CouponEntity(
                    code = "HOSGELDIN100",
                    discountType = "FIXED_TRY",
                    discountValue = 100.0,
                    startDate = now - 86400000L,
                    endDate = oneYearLater,
                    totalUsageLimit = 200,
                    perUserLimit = 1,
                    usedCount = 0,
                    isActive = true
                ),
                CouponEntity(
                    code = "YILLIK30",
                    discountType = "PERCENTAGE",
                    discountValue = 30.0,
                    startDate = now - 86400000L,
                    endDate = oneYearLater,
                    totalUsageLimit = 100,
                    perUserLimit = 1,
                    usedCount = 0,
                    isActive = true,
                    applicableBillingPeriod = "YEARLY"
                )
            )
            demoCoupons.forEach { db.couponDao().insertCoupon(it) }
        }

        if (userDao.getUserById("c1") != null) return


        val coach = User(
            id = "c1",
            name = "Demo Koç",
            email = "demokoc@mahfaza.co",
            role = UserRole.COACH,
            packageId = PackageCatalog.COACH_PRO.id,
            studentCapacity = 25,
            assignedStudents = listOf("s1", "s2"),
            title = "Eğitim Koçu & Akademik Danışman",
            bio = "10 yıllık YKS ve LGS koçluk tecrübesiyle öğrencilerimizin hedeflerine ulaşmasını sağlıyoruz.",
            coachingApproach = "Bireysel çalışma stiline özel, veriye dayalı ve disiplinli takip sistemi.",
            expertiseTags = listOf("LGS", "YKS", "Eğitim Koçluğu", "Deneme Analizi"),
            education = listOf(
                EducationEntry(UUID.randomUUID().toString(), "Boğaziçi Üniversitesi", "Rehberlik ve Psikolojik Danışmanlık", "Lisans", "2015")
            ),
            experience = listOf(
                ExperienceEntry(UUID.randomUUID().toString(), "Kıdemli Eğitim Koçu", "Mahfaza.co", "2020 - Günümüz")
            ),
            certificates = listOf(
                CertificateEntry(UUID.randomUUID().toString(), "Uluslararası Profesyonel Koçluk Sertifikası", "ICF", "2018")
            ),
            workingDays = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma"),
            workingHours = "09:00 - 18:00",
            acceptingStudents = true,
            coachCode = "KOC-DEMO1"
        )
        userDao.insertUser(coach.toEntity())

        val admin = User(
            id = "admin1",
            name = "Demo Yönetici",
            email = "admin@mahfaza.com",
            role = UserRole.ADMIN,
            packageId = PackageCatalog.COACH_PRO.id
        )
        userDao.insertUser(admin.toEntity())

        val students = listOf(
            User("s1", "Demo Öğrenci YKS", "yks@mahfaza.co", UserRole.STUDENT, EducationLevel.YKS_TYT_AYT, PackageCatalog.STUDENT_PRO.id, 1250, 2500, 12, assignedCoachId = "c1"),
            User("s2", "Demo Öğrenci LGS", "lgs@mahfaza.co", UserRole.STUDENT, EducationLevel.LGS_8TH, PackageCatalog.STUDENT_PLUS.id, 850, 1500, 8, assignedCoachId = "c1")
        )
        students.forEach { userDao.insertUser(it.toEntity()) }

        val rewards = listOf(
            Reward("r1", "1 Saat Oyun Molası", "Koçunla kararlaştırdığın bir saatte oyun oynayabilirsin.", 500, 10),
            Reward("r2", "Kitap Hediye Çeki", "100 TL değerinde kitap alışverişi çeki.", 1500, 5),
            Reward("r3", "Yemek Ismarlama", "Koçun sana istediğin bir menüyü ısmarlar.", 3000, 2)
        )
        rewards.forEach { rewardDao.insertReward(it.toEntity()) }

        // Seed Package Configs
        if (db.packageConfigDao().getAllPackageConfigsSync().isEmpty()) {
            val studentPackages = PackageCatalog.getAllStudentPackages().map { pkg ->
                PackageConfigEntity(pkg.id, pkg.name, pkg.price, pkg.yearlyPrice, true, pkg.studentCapacity)
            }
            val coachPackages = PackageCatalog.getAllCoachPackages().map { pkg ->
                PackageConfigEntity(pkg.id, pkg.name, pkg.price, pkg.yearlyPrice, true, pkg.studentCapacity)
            }
            (studentPackages + coachPackages).forEach { db.packageConfigDao().insertPackageConfig(it) }
        }



        val elaExams = listOf(
            ExamResult("e1", "Deneme 1", "2024-03-01", EducationLevel.LGS_8TH, listOf(SubjectResult("Matematik", 15, 5, 0, 13.75))),
            ExamResult("e2", "Deneme 2", "2024-03-15", EducationLevel.LGS_8TH, listOf(SubjectResult("Matematik", 18, 2, 0, 17.5))),
            ExamResult("e3", "Deneme 3", "2024-04-01", EducationLevel.LGS_8TH, listOf(SubjectResult("Matematik", 20, 0, 0, 20.0)))
        )
        elaExams.forEach { addExam("s2", it) }

        taskDao.insertTask(Task("t1", "Üslü Sayılar Soru Çözümü", "Matematik", "Üslü Sayılar", 50, 50, true, TaskPriority.HIGH, 50, 1, isApproved = true).toEntity("s2"))
        taskDao.insertTask(Task("t2", "Paragrafta Anlam Çalışması", "Türkçe", "Paragraf", 30, 0, false, TaskPriority.MEDIUM, 30, 1).toEntity("s2"))
        taskDao.insertTask(Task("t4", "Mevsimler ve İklim Analizi", "Fen Bilimleri", "Mevsimler", 40, 40, true, TaskPriority.HIGH, 35, 2, photoProofUri = "content://media/external/images/proof_t4.jpg", isApproved = false).toEntity("s2"))

        taskDao.insertTask(Task("t3", "Türev Başlangıç ve Formüller", "Matematik", "Türev", 40, 40, true, TaskPriority.HIGH, 40, 1, photoProofUri = "content://media/external/images/proof_t3.jpg", isApproved = false).toEntity("s1"))

        rewardRequestDao.insertRewardRequest(RewardRequestEntity("req1", "s2", "Ela Korkmaz", "r1", "1 Saat Oyun Molası", 500, System.currentTimeMillis() - 7200000, RewardRequestStatus.PENDING))

        sendNotification("s1", NotificationType.LEVEL_UP, "Tebrikler! Seviye 9'a Ulaştın!", "Yeni Unvanın: Mahfaza Ustası", idempotencyKey = "INIT_S1_LVL")
        sendNotification("s2", NotificationType.LEVEL_UP, "Tebrikler! Seviye 6'ya Ulaştın!", "Yeni Unvanın: Odak Ustası", idempotencyKey = "INIT_S2_LVL")
        sendNotification("c1", NotificationType.STUDY_SUBMITTED, "Yeni Çalışma Onayı", "Demo Öğrenci YKS çalışma kaydı gönderdi.", idempotencyKey = "INIT_C1_STUDY")

        messageDao.insertMessage(MessageEntity("m2", "s1", "c1", "Evet hocam, bitirdim ve sisteme yükledim. Analizleri bekliyorum.", System.currentTimeMillis() - 1800000, true))
    }

    suspend fun deleteUserCompletely(userId: String) {
        db.withTransaction {
            taskDao.deleteTasksByUserId(userId)
            db.examDao().deleteExamsByUserId(userId)
            db.pomodoroDao().deletePomodoroSessionsByStudentId(userId)
            db.xpDao().deleteXPTransactionsByUserId(userId)
            db.notificationDao().deleteNotificationsByUserId(userId)
            db.rewardRequestDao().deleteRewardRequestsByStudentId(userId)
            db.parentMeetingDao().deleteMeetingsByStudentId(userId)
            db.incomeDao().deleteIncomesByUserId(userId)
            db.messageDao().deleteMessagesByUserId(userId)
            userDao.deleteUserById(userId)
        }
    }

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()

    suspend fun addExpense(expense: ExpenseEntity) = db.expenseDao().insertExpense(expense)

    suspend fun deleteExpense(expenseId: String) = db.expenseDao().deleteExpenseById(expenseId)

    // Income
    fun getAllIncomes(): Flow<List<IncomeEntity>> = db.incomeDao().getAllIncomes()
    fun getTotalIncomeFlow(): Flow<Double?> = db.incomeDao().getTotalIncomeFlow()
    suspend fun addIncome(income: IncomeEntity) = db.incomeDao().insertIncome(income)
    suspend fun deleteIncome(income: IncomeEntity) = db.incomeDao().deleteIncome(income)

    // Package Config
    fun getAllPackageConfigs(): Flow<List<PackageConfigEntity>> = db.packageConfigDao().getAllPackageConfigs()
    suspend fun updatePackageConfig(config: PackageConfigEntity) = db.packageConfigDao().updatePackageConfig(config)
    suspend fun addPackageConfig(config: PackageConfigEntity) = db.packageConfigDao().insertPackageConfig(config)

    // --- COUPON SYSTEM ---
    fun getAllCoupons(): Flow<List<CouponEntity>> = db.couponDao().getAllCoupons()

    suspend fun getCouponByCode(code: String): CouponEntity? = db.couponDao().getCouponByCode(code)

    suspend fun addCoupon(coupon: CouponEntity) {
        val uppercaseCode = coupon.code.trim().uppercase()
        db.couponDao().insertCoupon(coupon.copy(code = uppercaseCode))
    }

    suspend fun updateCoupon(coupon: CouponEntity) {
        val uppercaseCode = coupon.code.trim().uppercase()
        db.couponDao().updateCoupon(coupon.copy(code = uppercaseCode))
    }

    suspend fun deleteCoupon(code: String) {
        db.couponDao().deleteCouponByCode(code)
    }

    suspend fun toggleCouponActiveStatus(code: String): Boolean {
        val coupon = db.couponDao().getCouponByCode(code) ?: return false
        val updated = coupon.copy(isActive = !coupon.isActive)
        db.couponDao().updateCoupon(updated)
        return true
    }

    suspend fun validateCoupon(
        code: String,
        userId: String,
        packageId: String,
        billingPeriod: String,
        originalPrice: Double
    ): CouponValidationResult {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isBlank()) {
            return CouponValidationResult.Error("Lütfen bir indirim kodu giriniz.")
        }

        val coupon = db.couponDao().getCouponByCode(cleanCode)
            ?: return CouponValidationResult.Error("Geçersiz veya bulunamayan indirim kodu.")

        if (!coupon.isActive) {
            return CouponValidationResult.Error("Bu indirim kodu pasif durumdadır.")
        }

        val now = System.currentTimeMillis()
        if (now < coupon.startDate || now > coupon.endDate) {
            return CouponValidationResult.Error("Bu indirim kodunun geçerlilik süresi dolmuştur.")
        }

        if (coupon.usedCount >= coupon.totalUsageLimit) {
            return CouponValidationResult.Error("Bu indirim kodunun toplam kullanım limiti dolmuştur.")
        }

        val userUsageCount = db.couponDao().getUserCouponUsageCount(cleanCode, userId)
        if (userUsageCount >= coupon.perUserLimit) {
            return CouponValidationResult.Error("Bu indirim kodunu daha önce kullandınız.")
        }

        if (coupon.applicablePackageId != null && !coupon.applicablePackageId.equals(packageId, ignoreCase = true)) {
            return CouponValidationResult.Error("Bu indirim kodu seçtiğiniz paket için geçerli değildir.")
        }

        if (coupon.applicableBillingPeriod != null && !coupon.applicableBillingPeriod.equals(billingPeriod, ignoreCase = true)) {
            return CouponValidationResult.Error("Bu indirim kodu seçtiğiniz ödeme periyodu ($billingPeriod) için geçerli değildir.")
        }

        val rawDiscount = if (coupon.discountType.equals("PERCENTAGE", ignoreCase = true)) {
            originalPrice * (coupon.discountValue / 100.0)
        } else {
            coupon.discountValue
        }

        val discountAmount = minOf(rawDiscount, originalPrice)
        val finalPrice = (originalPrice - discountAmount).coerceAtLeast(0.0)

        val description = if (coupon.discountType.equals("PERCENTAGE", ignoreCase = true)) {
            "%${coupon.discountValue.toInt()} İndirim Uygulandı"
        } else {
            "${coupon.discountValue.toInt()} TL İndirim Uygulandı"
        }

        return CouponValidationResult.Success(
            coupon = coupon,
            discountAmount = discountAmount,
            finalPrice = finalPrice,
            description = description
        )
    }

    suspend fun processTestPayment(
        userId: String,
        packageId: String,
        billingPeriod: String,
        appliedCouponCode: String? = null,
        originalPrice: Double
    ): Boolean {
        return db.withTransaction {
            val user = userDao.getUserById(userId) ?: return@withTransaction false
            var discountAmount = 0.0
            var finalPrice = originalPrice

            if (!appliedCouponCode.isNullOrBlank()) {
                val validation = validateCoupon(appliedCouponCode, userId, packageId, billingPeriod, originalPrice)
                if (validation is CouponValidationResult.Success) {
                    discountAmount = validation.discountAmount
                    finalPrice = validation.finalPrice

                    val c = validation.coupon
                    db.couponDao().updateCoupon(c.copy(usedCount = c.usedCount + 1))

                    db.couponDao().insertCouponUsage(
                        CouponUsageEntity(
                            id = UUID.randomUUID().toString(),
                            couponCode = c.code,
                            userId = userId,
                            usedAt = System.currentTimeMillis(),
                            packageId = packageId,
                            discountAmount = discountAmount
                        )
                    )
                } else {
                    return@withTransaction false
                }
            }

            val planName = if (billingPeriod.equals("YEARLY", ignoreCase = true)) "PAID_YEARLY" else "PAID_MONTHLY"
            val pkg = PackageCatalog.getPackageById(packageId)
            val updatedUser = user.copy(
                packageId = packageId,
                subscriptionPlan = planName,
                studentCapacity = if (user.role == UserRole.COACH) pkg?.studentCapacity ?: user.studentCapacity else user.studentCapacity
            )
            userDao.updateUser(updatedUser)

            if (_currentUser.value?.id == userId) {
                _currentUser.value = updatedUser.toModel()
            }

            db.incomeDao().insertIncome(
                IncomeEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    amount = finalPrice,
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                    description = "Test Ödemesi - Paket: $packageId ($billingPeriod)" + if (!appliedCouponCode.isNullOrBlank()) " [Kupon: ${appliedCouponCode.uppercase()}]" else "",
                    packageId = packageId,
                    createdByAdminId = "SYSTEM_TEST_PAYMENT"
                )
            )

            true
        }
    }


    // Backup & Restore
    suspend fun getBackupData(): String {
        val backup = DatabaseBackup(
            schemaVersion = 14,
            createdAt = System.currentTimeMillis(),
            users = userDao.getAllUsersSync(),
            tasks = taskDao.getAllTasksSync(),
            examResults = db.examDao().getAllExamsSync(),
            messages = db.messageDao().getAllMessagesSync(),
            xpTransactions = db.xpDao().getAllXPTransactionsSync(),
            notifications = db.notificationDao().getAllNotificationsSync(),
            rewards = db.rewardDao().getAllRewardsSync(),
            rewardRequests = db.rewardRequestDao().getAllRewardRequestsSync(),
            parentMeetings = db.parentMeetingDao().getAllMeetingsSync(),
            pomodoroSessions = db.pomodoroDao().getAllPomodoroSessionsSync(),
            expenses = db.expenseDao().getAllExpensesSync(),
            packageConfigs = db.packageConfigDao().getAllPackageConfigsSync(),
            incomes = db.incomeDao().getAllIncomesSync(),
            reminderSettings = db.reminderSettingsDao().getAllReminderSettingsSync(),
            coupons = db.couponDao().getAllCouponsSync(),
            couponUsages = db.couponDao().getAllCouponUsagesSync()
        )
        return Json.encodeToString(backup)
    }

    suspend fun restoreDatabaseFromJson(json: String): Boolean {
        return try {
            val backup = Json.decodeFromString<DatabaseBackup>(json)
            if (backup.schemaVersion < 11) return false

            db.withTransaction {
                db.clearAllTables()

                for (it in backup.users) userDao.insertUser(it)
                for (it in backup.tasks) taskDao.insertTask(it)
                for (it in backup.examResults) db.examDao().insertExam(it)
                for (it in backup.messages) db.messageDao().insertMessage(it)
                for (it in backup.xpTransactions) db.xpDao().insertXPTransaction(it)
                for (it in backup.notifications) db.notificationDao().insertNotification(it)
                for (it in backup.rewards) db.rewardDao().insertReward(it)
                for (it in backup.rewardRequests) db.rewardRequestDao().insertRewardRequest(it)
                for (it in backup.parentMeetings) db.parentMeetingDao().insertMeeting(it)
                for (it in backup.pomodoroSessions) db.pomodoroDao().insertPomodoroSession(it)
                for (it in backup.expenses) db.expenseDao().insertExpense(it)
                for (it in backup.packageConfigs) db.packageConfigDao().insertPackageConfig(it)
                for (it in backup.incomes) db.incomeDao().insertIncome(it)
                backup.reminderSettings?.let { settingsList ->
                    for (it in settingsList) db.reminderSettingsDao().insertReminderSettings(it)
                }
                backup.coupons?.let { couponList ->
                    for (it in couponList) db.couponDao().insertCoupon(it)
                }
                backup.couponUsages?.let { usageList ->
                    for (it in usageList) db.couponDao().insertCouponUsage(it)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

@Serializable
data class DatabaseBackup(
    val schemaVersion: Int,
    val createdAt: Long,
    val users: List<UserEntity>,
    val tasks: List<TaskEntity>,
    val examResults: List<ExamResultEntity>,
    val messages: List<MessageEntity>,
    val xpTransactions: List<XPTransactionEntity>,
    val notifications: List<NotificationEntity>,
    val rewards: List<RewardEntity>,
    val rewardRequests: List<RewardRequestEntity>,
    val parentMeetings: List<ParentMeetingEntity>,
    val pomodoroSessions: List<PomodoroSessionEntity>,
    val expenses: List<ExpenseEntity>,
    val packageConfigs: List<PackageConfigEntity>,
    val incomes: List<IncomeEntity>,
    val reminderSettings: List<ReminderSettingsEntity>? = null,
    val coupons: List<CouponEntity>? = null,
    val couponUsages: List<CouponUsageEntity>? = null
)

