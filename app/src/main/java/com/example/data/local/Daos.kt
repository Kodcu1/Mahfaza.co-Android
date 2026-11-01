package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'STUDENT'")
    fun getAllStudents(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'COACH'")
    fun getAllCoaches(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE coachCode = :code")
    suspend fun getUserByCoachCode(code: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT * FROM users")
    suspend fun getAllUsersSync(): List<UserEntity>

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: String)

    @Query("SELECT COUNT(*) FROM users WHERE role = 'STUDENT'")
    suspend fun countStudents(): Int

    @Query("SELECT COUNT(*) FROM users WHERE role = 'COACH'")
    suspend fun countCoaches(): Int

    @Query("SELECT COUNT(*) FROM users WHERE packageId != 'std_free'")
    suspend fun countActivePackageUsers(): Int
}

@Dao
interface PomodoroDao {
    @Query("SELECT * FROM pomodoro_sessions WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getPomodoroSessions(studentId: String): Flow<List<PomodoroSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPomodoroSession(session: PomodoroSessionEntity)

    @Query("SELECT * FROM pomodoro_sessions")
    suspend fun getAllPomodoroSessionsSync(): List<PomodoroSessionEntity>

    @Query("DELETE FROM pomodoro_sessions WHERE studentId = :studentId")
    suspend fun deletePomodoroSessionsByStudentId(studentId: String)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId")
    fun getTasksByUserId(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countAll(): Int

    @Query("SELECT * FROM tasks WHERE (photoProofUri IS NOT NULL OR isCompleted = 1) AND isApproved = 0 AND isRejected = 0")
    fun getPendingApprovalTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isApproved = 1 AND isArchived = 0")
    fun getApprovedTasks(): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET isArchived = 1 WHERE id = :taskId")
    suspend fun archiveTask(taskId: String)

    @Query("UPDATE tasks SET isArchived = 1 WHERE isApproved = 1 AND isArchived = 0")
    suspend fun archiveAllApprovedTasks()

    @Query("SELECT * FROM tasks WHERE isRejected = 1")
    fun getRejectedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasksSync(): List<TaskEntity>

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteTasksByUserId(userId: String)
}

@Dao
interface ExamDao {
    @Query("SELECT * FROM exam_results WHERE userId = :userId ORDER BY date DESC")
    fun getExamsByUserId(userId: String): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamResultEntity)

    @Query("SELECT * FROM exam_results")
    suspend fun getAllExamsSync(): List<ExamResultEntity>

    @Query("DELETE FROM exam_results WHERE userId = :userId")
    suspend fun deleteExamsByUserId(userId: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getChatMessages(userId: String, otherId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages")
    suspend fun getAllMessagesSync(): List<MessageEntity>

    @Query("DELETE FROM messages WHERE senderId = :userId OR receiverId = :userId")
    suspend fun deleteMessagesByUserId(userId: String)
}

@Dao
interface XPDao {
    @Query("SELECT * FROM xp_transactions WHERE userId = :userId ORDER BY timestamp DESC")
    fun getXPTransactionsByUserId(userId: String): Flow<List<XPTransactionEntity>>

    @Query("SELECT * FROM xp_transactions WHERE userId = :userId AND sourceReference = :sourceRef LIMIT 1")
    suspend fun getTransactionBySource(userId: String, sourceRef: String): XPTransactionEntity?

    @Query("SELECT SUM(amount) FROM xp_transactions WHERE userId = :userId AND type = 'EARNED' AND timestamp >= :startOfDayTimestamp AND timestamp < :endOfDayTimestamp")
    suspend fun getEarnedXpToday(userId: String, startOfDayTimestamp: Long, endOfDayTimestamp: Long): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXPTransaction(transaction: XPTransactionEntity)

    @Query("SELECT * FROM xp_transactions")
    suspend fun getAllXPTransactionsSync(): List<XPTransactionEntity>

    @Query("DELETE FROM xp_transactions WHERE userId = :userId")
    suspend fun deleteXPTransactionsByUserId(userId: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE recipientId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE recipientId = :userId AND isRead = 0")
    fun getUnreadCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientId = :userId")
    suspend fun markAllAsRead(userId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotificationsSync(): List<NotificationEntity>

    @Query("DELETE FROM notifications WHERE recipientId = :userId")
    suspend fun deleteNotificationsByUserId(userId: String)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards")
    fun getAllRewards(): Flow<List<RewardEntity>>

    @Query("SELECT * FROM rewards WHERE id = :id")
    suspend fun getRewardById(id: String): RewardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardEntity)

    @Update
    suspend fun updateReward(reward: RewardEntity)

    @Delete
    suspend fun deleteReward(reward: RewardEntity)

    @Query("DELETE FROM rewards WHERE id = :id")
    suspend fun deleteRewardById(id: String)

    @Query("SELECT * FROM rewards")
    suspend fun getAllRewardsSync(): List<RewardEntity>
}

@Dao
interface RewardRequestDao {
    @Query("SELECT * FROM reward_requests ORDER BY timestamp DESC")
    fun getAllRewardRequests(): Flow<List<RewardRequestEntity>>

    @Query("SELECT * FROM reward_requests WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getRewardRequestsByStudentId(studentId: String): Flow<List<RewardRequestEntity>>

    @Query("SELECT * FROM reward_requests WHERE id = :id")
    suspend fun getRewardRequestById(id: String): RewardRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardRequest(request: RewardRequestEntity)

    @Update
    suspend fun updateRewardRequest(request: RewardRequestEntity)

    @Query("SELECT * FROM reward_requests")
    suspend fun getAllRewardRequestsSync(): List<RewardRequestEntity>

    @Query("DELETE FROM reward_requests WHERE studentId = :studentId")
    suspend fun deleteRewardRequestsByStudentId(studentId: String)
}

@Dao
interface ParentMeetingDao {
    @Query("SELECT * FROM parent_meetings WHERE studentId = :studentId ORDER BY date DESC")
    fun getMeetingsByStudentId(studentId: String): Flow<List<ParentMeetingEntity>>

    @Query("SELECT * FROM parent_meetings ORDER BY date DESC")
    fun getAllMeetings(): Flow<List<ParentMeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: ParentMeetingEntity)

    @Update
    suspend fun updateMeeting(meeting: ParentMeetingEntity)

    @Delete
    suspend fun deleteMeeting(meeting: ParentMeetingEntity)

    @Query("SELECT * FROM parent_meetings")
    suspend fun getAllMeetingsSync(): List<ParentMeetingEntity>

    @Query("DELETE FROM parent_meetings WHERE studentId = :studentId")
    suspend fun deleteMeetingsByStudentId(studentId: String)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: String)

    @Query("SELECT * FROM expenses")
    suspend fun getAllExpensesSync(): List<ExpenseEntity>
}

@Dao
interface PackageConfigDao {
    @Query("SELECT * FROM package_configs")
    fun getAllPackageConfigs(): Flow<List<PackageConfigEntity>>

    @Query("SELECT * FROM package_configs WHERE id = :id")
    suspend fun getPackageConfigById(id: String): PackageConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackageConfig(config: PackageConfigEntity)

    @Update
    suspend fun updatePackageConfig(config: PackageConfigEntity)

    @Query("SELECT * FROM package_configs")
    suspend fun getAllPackageConfigsSync(): List<PackageConfigEntity>
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("SELECT SUM(amount) FROM incomes")
    fun getTotalIncomeFlow(): Flow<Double?>

    @Query("SELECT * FROM incomes")
    suspend fun getAllIncomesSync(): List<IncomeEntity>

    @Query("DELETE FROM incomes WHERE userId = :userId")
    suspend fun deleteIncomesByUserId(userId: String)
}

@Dao
interface ReminderSettingsDao {
    @Query("SELECT * FROM reminder_settings WHERE userId = :userId")
    fun getReminderSettings(userId: String): Flow<ReminderSettingsEntity?>

    @Query("SELECT * FROM reminder_settings WHERE userId = :userId")
    suspend fun getReminderSettingsSync(userId: String): ReminderSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderSettings(settings: ReminderSettingsEntity)

    @Update
    suspend fun updateReminderSettings(settings: ReminderSettingsEntity)
    
    @Query("SELECT * FROM reminder_settings")
    suspend fun getAllReminderSettingsSync(): List<ReminderSettingsEntity>
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons WHERE UPPER(code) = UPPER(:code)")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Query("SELECT * FROM coupons ORDER BY code ASC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons")
    suspend fun getAllCouponsSync(): List<CouponEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Query("DELETE FROM coupons WHERE UPPER(code) = UPPER(:code)")
    suspend fun deleteCouponByCode(code: String)

    @Query("SELECT COUNT(*) FROM coupon_usages WHERE UPPER(couponCode) = UPPER(:couponCode) AND userId = :userId")
    suspend fun getUserCouponUsageCount(couponCode: String, userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCouponUsage(usage: CouponUsageEntity)

    @Query("SELECT * FROM coupon_usages")
    suspend fun getAllCouponUsagesSync(): List<CouponUsageEntity>
}

