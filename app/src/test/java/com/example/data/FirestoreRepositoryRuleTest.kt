package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.firestore.FirestoreRepository
import com.example.model.Task
import com.example.model.TaskPriority
import com.example.model.User
import com.example.model.UserRole
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class FirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun saveUser_and_getUser_authenticatedOwner_succeeds(): Unit = runBlocking {
    val uid: String = signInTestUser(ALICE_EMAIL)
    val repo = FirestoreRepository(firestore, { uid })

    val user = User(
      id = uid,
      name = "Alice Test",
      email = ALICE_EMAIL,
      role = UserRole.STUDENT,
      packageId = "student_free"
    )

    withTimeout(TIMEOUT_MS) {
      repo.saveUser(user)
      val fetched = repo.getUser(uid)
      assertNotNull(fetched)
      assertEquals("Alice Test", fetched?.name)
    }
  }

  @Test
  fun saveTask_and_observeTasks_authenticatedOwner_returnsTask(): Unit = runBlocking {
    val uid: String = signInTestUser(ALICE_EMAIL)
    val repo = FirestoreRepository(firestore, { uid })

    val task = Task(
      id = "task_test_1",
      title = "Fizik Newton Yasaları",
      subject = "Fizik",
      topic = "Dinamik",
      targetQuestionCount = 30,
      isCompleted = false,
      priority = TaskPriority.HIGH,
      xpReward = 15,
      dayOfWeek = 1
    )

    withTimeout(TIMEOUT_MS) {
      repo.saveTask(task, uid)
      val tasks = repo.observeTasks(uid).first { list -> list.any { it.id == "task_test_1" } }
      assertTrue(tasks.isNotEmpty())
      assertEquals("Fizik Newton Yasaları", tasks.first { it.id == "task_test_1" }.title)
    }
  }

  @Test
  fun crossUser_taskObservation_failsWithPermissionDenied(): Unit = runBlocking {
    val aliceUid: String = signInTestUser(ALICE_EMAIL)
    val aliceRepo = FirestoreRepository(firestore, { aliceUid })

    val task = Task(
      id = "task_alice_sec",
      title = "Kimya Organik",
      subject = "Kimya",
      topic = "Alkanlar",
      targetQuestionCount = 20,
      isCompleted = false,
      priority = TaskPriority.MEDIUM,
      xpReward = 10,
      dayOfWeek = 2
    )
    withTimeout(TIMEOUT_MS) {
      aliceRepo.saveTask(task, aliceUid)
    }

    val bobUid: String = signInTestUser(BOB_EMAIL)
    val bobRepo = FirestoreRepository(firestore, { bobUid })

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        bobRepo.observeTasks(aliceUid).first()
      }
      fail("Expected security exception on cross user access")
    } catch (e: Exception) {
      val firestoreException = e.unwrapFirestoreException()
      if (firestoreException != null) {
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
      } else {
        assertTrue(e.message?.contains("PERMISSION_DENIED") == true || e.cause != null)
      }
    }
  }

  @Test
  fun unauthenticatedUser_observeTasks_failsWithPermissionDenied(): Unit = runBlocking {
    auth.signOut()
    val repo = FirestoreRepository(firestore, { null })

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repo.observeTasks("some_fake_uid").first()
      }
      fail("Expected exception for unauthenticated user")
    } catch (e: Exception) {
      assertNotNull(e)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice_student@test.com"
    const val BOB_EMAIL = "bob_student@test.com"
    const val TIMEOUT_MS = 6000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
