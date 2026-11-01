package com.example.data.local

import com.example.model.*

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    name = name,
    email = email,
    role = role,
    educationLevel = educationLevel,
    packageId = packageId,
    currentXp = currentXp,
    lifetimeXp = lifetimeXp,
    streak = streak,
    studentCapacity = studentCapacity,
    assignedStudents = assignedStudents,
    title = title,
    bio = bio,
    coachingApproach = coachingApproach,
    expertiseTags = expertiseTags,
    education = education,
    experience = experience,
    certificates = certificates,
    workingDays = workingDays,
    workingHours = workingHours,
    acceptingStudents = acceptingStudents,
    avatarUri = avatarUri,
    coachCode = coachCode,
    targetUniversity = targetUniversity,
    targetDepartment = targetDepartment,
    targetRanking = targetRanking,
    targetCity = targetCity,
    targetExamDate = targetExamDate,
    dailyQuestionTarget = dailyQuestionTarget,
    dailyStudyTarget = dailyStudyTarget,
    goals = goals,
    assignedCoachId = assignedCoachId,
    examTracks = examTracks,
    subscriptionPlan = subscriptionPlan
)

fun UserEntity.toModel(): User = User(
    id = id,
    name = name,
    email = email,
    role = role,
    educationLevel = educationLevel,
    packageId = packageId,
    currentXp = currentXp,
    lifetimeXp = lifetimeXp,
    streak = streak,
    studentCapacity = studentCapacity,
    assignedStudents = assignedStudents,
    title = title,
    bio = bio,
    coachingApproach = coachingApproach,
    expertiseTags = expertiseTags,
    education = education,
    experience = experience,
    certificates = certificates,
    workingDays = workingDays,
    workingHours = workingHours,
    acceptingStudents = acceptingStudents,
    avatarUri = avatarUri,
    coachCode = coachCode,
    targetUniversity = targetUniversity,
    targetDepartment = targetDepartment,
    targetRanking = targetRanking,
    targetCity = targetCity,
    targetExamDate = targetExamDate,
    dailyQuestionTarget = dailyQuestionTarget,
    dailyStudyTarget = dailyStudyTarget,
    goals = goals,
    assignedCoachId = assignedCoachId,
    examTracks = examTracks,
    subscriptionPlan = subscriptionPlan
)

fun PomodoroSession.toEntity(): PomodoroSessionEntity = PomodoroSessionEntity(
    id = id,
    studentId = studentId,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    mode = mode,
    subject = subject
)

fun PomodoroSessionEntity.toModel(): PomodoroSession = PomodoroSession(
    id = id,
    studentId = studentId,
    durationMinutes = durationMinutes,
    timestamp = timestamp,
    mode = mode,
    subject = subject
)

fun Task.toEntity(userId: String): TaskEntity = TaskEntity(
    id = id,
    userId = userId,
    title = title,
    subject = subject,
    topic = topic,
    targetQuestionCount = targetQuestionCount,
    completedQuestionCount = completedQuestionCount,
    isCompleted = isCompleted,
    priority = priority,
    xpReward = xpReward,
    dayOfWeek = dayOfWeek,
    reminderTime = reminderTime,
    photoProofUri = photoProofUri,
    isApproved = isApproved,
    isRejected = isRejected
)

fun TaskEntity.toModel(): Task = Task(
    id = id,
    title = title,
    subject = subject,
    topic = topic,
    targetQuestionCount = targetQuestionCount,
    completedQuestionCount = completedQuestionCount,
    isCompleted = isCompleted,
    priority = priority,
    xpReward = xpReward,
    dayOfWeek = dayOfWeek,
    reminderTime = reminderTime,
    photoProofUri = photoProofUri,
    isApproved = isApproved,
    isRejected = isRejected
)

fun ExamResult.toEntity(userId: String): ExamResultEntity = ExamResultEntity(
    id = id,
    userId = userId,
    name = name,
    date = date,
    educationLevel = educationLevel,
    subjectResults = subjectResults
)

fun ExamResultEntity.toModel(): ExamResult = ExamResult(
    id = id,
    name = name,
    date = date,
    educationLevel = educationLevel,
    subjectResults = subjectResults
)

fun XPTransaction.toEntity(): XPTransactionEntity = XPTransactionEntity(
    id = id,
    userId = studentId,
    amount = amount,
    type = type,
    reason = reason,
    timestamp = timestamp,
    sourceReference = sourceReference
)

fun XPTransactionEntity.toModel(): XPTransaction = XPTransaction(
    id = id,
    studentId = userId,
    amount = amount,
    type = type,
    reason = reason,
    timestamp = timestamp,
    sourceReference = sourceReference
)

fun AppNotification.toEntity(): NotificationEntity = NotificationEntity(
    id = id,
    recipientId = recipientId,
    type = type,
    title = title,
    message = message,
    relatedEntityId = relatedEntityId,
    timestamp = timestamp,
    isRead = isRead,
    navigationRoute = navigationRoute,
    idempotencyKey = idempotencyKey
)

fun NotificationEntity.toModel(): AppNotification = AppNotification(
    id = id,
    recipientId = recipientId,
    type = type,
    title = title,
    message = message,
    relatedEntityId = relatedEntityId,
    timestamp = timestamp,
    isRead = isRead,
    navigationRoute = navigationRoute,
    idempotencyKey = idempotencyKey
)

fun Reward.toEntity(): RewardEntity = RewardEntity(
    id = id,
    name = name,
    description = description,
    xpCost = xpCost,
    stock = stock,
    imageUrl = imageUrl
)

fun RewardEntity.toModel(): Reward = Reward(
    id = id,
    name = name,
    description = description,
    xpCost = xpCost,
    stock = stock,
    imageUrl = imageUrl
)

fun RewardRequest.toEntity(): RewardRequestEntity = RewardRequestEntity(
    id = id,
    studentId = studentId,
    studentName = studentName,
    rewardId = rewardId,
    rewardName = rewardName,
    xpCost = xpCost,
    timestamp = timestamp,
    status = status
)

fun RewardRequestEntity.toModel(): RewardRequest = RewardRequest(
    id = id,
    studentId = studentId,
    studentName = studentName,
    rewardId = rewardId,
    rewardName = rewardName,
    xpCost = xpCost,
    timestamp = timestamp,
    status = status
)

fun ParentMeeting.toEntity(): ParentMeetingEntity = ParentMeetingEntity(
    id = id,
    studentId = studentId,
    studentName = studentName,
    date = date,
    notes = notes,
    isCompleted = isCompleted,
    topic = topic,
    decisions = decisions,
    nextMeetingDate = nextMeetingDate,
    subjectNotesJson = subjectNotesJson
)

fun ParentMeetingEntity.toModel(): ParentMeeting = ParentMeeting(
    id = id,
    studentId = studentId,
    studentName = studentName,
    date = date,
    notes = notes,
    isCompleted = isCompleted,
    topic = topic,
    decisions = decisions,
    nextMeetingDate = nextMeetingDate,
    subjectNotesJson = subjectNotesJson
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    title = title,
    category = category,
    amount = amount,
    currency = currency,
    date = date,
    description = description,
    createdByAdminId = createdByAdminId
)

fun ExpenseEntity.toModel(): Expense = Expense(
    id = id,
    title = title,
    category = category,
    amount = amount,
    currency = currency,
    date = date,
    description = description,
    createdByAdminId = createdByAdminId
)

fun PackageConfig.toEntity(): PackageConfigEntity = PackageConfigEntity(
    id = id,
    name = name,
    price = price,
    yearlyPrice = yearlyPrice,
    isActive = isActive,
    studentCapacity = studentCapacity
)

fun PackageConfigEntity.toModel(): PackageConfig = PackageConfig(
    id = id,
    name = name,
    price = price,
    yearlyPrice = yearlyPrice,
    isActive = isActive,
    studentCapacity = studentCapacity
)

fun Income.toEntity(): IncomeEntity = IncomeEntity(
    id = id,
    userId = userId,
    amount = amount,
    date = date,
    description = description,
    packageId = packageId,
    createdByAdminId = createdByAdminId
)

fun IncomeEntity.toModel(): Income = Income(
    id = id,
    userId = userId,
    amount = amount,
    date = date,
    description = description,
    packageId = packageId,
    createdByAdminId = createdByAdminId
)
