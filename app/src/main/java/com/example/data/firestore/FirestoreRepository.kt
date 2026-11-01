package com.example.data.firestore

import android.content.Context
import com.example.R
import com.example.data.OperationType
import com.example.data.handleFirestoreError
import com.example.model.*
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreRepository(
    private val db: FirebaseFirestore,
    private val currentUserIdProvider: () -> String? = {
        try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (_: Exception) {
            null
        }
    }
) {
    constructor(context: Context) : this(
        try {
            val app = if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(
                    context,
                    com.google.firebase.FirebaseOptions.Builder()
                        .setApplicationId("com.aistudio.applet.kkxhhk")
                        .setProjectId("gen-lang-client-0970085058")
                        .setApiKey("AIzaSyTestingKey1234567890ABCDEF")
                        .build()
                )
            } else {
                com.google.firebase.FirebaseApp.getInstance()
            }
            val dbId = try {
                context.applicationContext.getString(R.string.firestore_database_id)
            } catch (_: Exception) {
                ""
            }
            if (dbId.isNotBlank()) {
                FirebaseFirestore.getInstance(app, dbId)
            } else {
                FirebaseFirestore.getInstance(app)
            }
        } catch (_: Exception) {
            FirebaseFirestore.getInstance()
        }
    )

    fun requireUserId(): String {
        return currentUserIdProvider()
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun isUserAdmin(email: String?): Boolean {
        if (email.isNullOrBlank()) return false
        val clean = email.trim().lowercase()
        return clean == "serkankocak551@gmail.com"
    }

    suspend fun saveUser(user: User) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mutableMapOf<String, Any>(
            "id" to uid,
            "name" to user.name,
            "email" to user.email,
            "role" to (if (isUserAdmin(user.email)) "ADMIN" else user.role.name),
            "currentXp" to user.currentXp,
            "lifetimeXp" to user.lifetimeXp,
            "streak" to user.streak,
            "studentCapacity" to user.studentCapacity,
            "packageId" to user.packageId,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        user.educationLevel?.name?.let { payload["educationLevel"] = it }
        user.assignedCoachId?.let { payload["assignedCoachId"] = it }

        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun getUser(userId: String): User? {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            if (!snapshot.exists()) return null
            val fsUser = snapshot.toObject(FsUser::class.java) ?: return null
            val role = try { UserRole.valueOf(fsUser.role) } catch (_: Exception) { UserRole.STUDENT }
            val level = fsUser.educationLevel?.let {
                try { EducationLevel.valueOf(it) } catch (_: Exception) { null }
            }
            User(
                id = fsUser.id,
                name = fsUser.name,
                email = fsUser.email,
                role = if (isUserAdmin(fsUser.email)) UserRole.ADMIN else role,
                educationLevel = level,
                packageId = fsUser.packageId,
                currentXp = fsUser.currentXp.toInt(),
                lifetimeXp = fsUser.lifetimeXp.toInt(),
                streak = fsUser.streak.toInt(),
                studentCapacity = fsUser.studentCapacity.toInt(),
                assignedCoachId = fsUser.assignedCoachId
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            throw e
        }
    }

    fun observeTasks(userId: String = requireUserId()): Flow<List<Task>> {
        val path = "users/$userId/tasks"
        return db.collection("users").document(userId).collection("tasks")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FsTask::class.java).map { fsTask ->
                    val priority = try { TaskPriority.valueOf(fsTask.priority) } catch (_: Exception) { TaskPriority.MEDIUM }
                    Task(
                        id = fsTask.id,
                        title = fsTask.title,
                        subject = fsTask.subject,
                        topic = fsTask.topic,
                        targetQuestionCount = fsTask.targetQuestionCount.toInt(),
                        completedQuestionCount = fsTask.completedQuestionCount.toInt(),
                        isCompleted = fsTask.isCompleted,
                        priority = priority,
                        xpReward = fsTask.xpReward.toInt(),
                        dayOfWeek = fsTask.dayOfWeek.toInt(),
                        isApproved = fsTask.isApproved,
                        isRejected = fsTask.isRejected
                    )
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun saveTask(task: Task, userId: String = requireUserId()): String {
        val tasksRef = db.collection("users").document(userId).collection("tasks")
        val docId = if (task.id.isNotBlank()) task.id else tasksRef.document().id
        val docRef = tasksRef.document(docId)

        val payload = mapOf(
            "id" to docId,
            "userId" to userId,
            "title" to task.title,
            "subject" to task.subject,
            "topic" to task.topic,
            "targetQuestionCount" to task.targetQuestionCount,
            "completedQuestionCount" to task.completedQuestionCount,
            "isCompleted" to task.isCompleted,
            "priority" to task.priority.name,
            "xpReward" to task.xpReward,
            "dayOfWeek" to task.dayOfWeek,
            "isApproved" to task.isApproved,
            "isRejected" to task.isRejected,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun updateTaskStatus(taskId: String, isCompleted: Boolean, completedQuestions: Int, userId: String = requireUserId()) {
        val docRef = db.collection("users").document(userId).collection("tasks").document(taskId)
        try {
            docRef.update(
                mapOf(
                    "isCompleted" to isCompleted,
                    "completedQuestionCount" to completedQuestions,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteTask(taskId: String, userId: String = requireUserId()) {
        val docRef = db.collection("users").document(userId).collection("tasks").document(taskId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    fun observeExams(userId: String = requireUserId()): Flow<List<ExamResult>> {
        val path = "users/$userId/exams"
        return db.collection("users").document(userId).collection("exams")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(FsExamResult::class.java).map { fsExam ->
                    val level = try {
                        EducationLevel.valueOf(fsExam.educationLevel)
                    } catch (_: Exception) {
                        EducationLevel.YKS_TYT_AYT
                    }
                    ExamResult(
                        id = fsExam.id,
                        name = fsExam.name,
                        date = fsExam.date,
                        educationLevel = level,
                        subjectResults = emptyList()
                    )
                }
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                throw error
            }
    }

    suspend fun addExam(exam: ExamResult, userId: String = requireUserId()): String {
        val examsRef = db.collection("users").document(userId).collection("exams")
        val docId = if (exam.id.isNotBlank()) exam.id else examsRef.document().id
        val docRef = examsRef.document(docId)

        val payload = mapOf(
            "id" to docId,
            "userId" to userId,
            "name" to exam.name,
            "date" to exam.date,
            "educationLevel" to exam.educationLevel.name,
            "totalNet" to exam.subjectResults.sumOf { it.net },
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }

    suspend fun logXPTransaction(transaction: XPTransaction, userId: String = requireUserId()): String {
        val txRef = db.collection("users").document(userId).collection("xp_transactions")
        val docId = if (transaction.id.isNotBlank()) transaction.id else txRef.document().id
        val docRef = txRef.document(docId)

        val payload = mapOf(
            "id" to docId,
            "userId" to userId,
            "amount" to transaction.amount,
            "type" to transaction.type.name,
            "reason" to transaction.reason,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            return docId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            throw e
        }
    }
}
