package com.example.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FsUser(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "STUDENT",
    val educationLevel: String? = null,
    val packageId: String = "student_free",
    val currentXp: Long = 0L,
    val lifetimeXp: Long = 0L,
    val streak: Long = 0L,
    val studentCapacity: Long = 0L,
    val assignedCoachId: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FsTask(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val subject: String = "",
    val topic: String = "",
    val targetQuestionCount: Long = 0L,
    val completedQuestionCount: Long = 0L,
    val isCompleted: Boolean = false,
    val priority: String = "MEDIUM",
    val xpReward: Long = 10L,
    val dayOfWeek: Long = 1L,
    val isApproved: Boolean = false,
    val isRejected: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FsExamResult(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val date: String = "",
    val educationLevel: String = "YKS_TYT_AYT",
    val totalNet: Double = 0.0,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FsXPTransaction(
    val id: String = "",
    val userId: String = "",
    val amount: Long = 0L,
    val type: String = "EARNED",
    val reason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FsReward(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val xpCost: Long = 0L,
    val stock: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FsRewardRequest(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val rewardId: String = "",
    val rewardName: String = "",
    val xpCost: Long = 0L,
    val status: String = "PENDING",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
