package com.example.base

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
@LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
abstract class FirestoreEmulatorTestBase {

  protected lateinit var firestore: FirebaseFirestore
  protected lateinit var auth: FirebaseAuth
  protected lateinit var databaseId: String

  @Before
  open fun setUpFirebase() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    databaseId = try {
      context.getString(com.example.R.string.firestore_database_id)
    } catch (_: Exception) {
      "ai-studio-android-remixmah-f2d42237-23dc-4255-a5d5-c1c010307488"
    }

    val app = if (FirebaseApp.getApps(context).isEmpty()) {
      FirebaseApp.initializeApp(
        context,
        FirebaseOptions.Builder()
          .setApplicationId("com.aistudio.applet.kkxhhk")
          .setProjectId(PROJECT_ID)
          .setApiKey("fake-api-key-for-emulator")
          .build()
      )
    } else {
      FirebaseApp.getInstance()
    }

    auth = FirebaseAuth.getInstance(app)
    try {
      auth.useEmulator(EMULATOR_HOST, AUTH_PORT)
    } catch (_: Exception) {}

    firestore = FirebaseFirestore.getInstance(app, databaseId)
    try {
      firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
        .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
        .build()
    } catch (_: Exception) {}
    try {
      firestore.useEmulator(EMULATOR_HOST, FIRESTORE_PORT)
    } catch (_: Exception) {}
  }

  @After
  open fun tearDownFirebase() {
    try {
      auth.signOut()
    } catch (_: Exception) {}
  }

  protected suspend fun signInTestUser(email: String): String = withContext(Dispatchers.IO) {
    withTimeout(AUTH_TIMEOUT_MS) {
      val result = try {
        auth.signInWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
      } catch (unused: Exception) {
        auth.createUserWithEmailAndPassword(email, DEFAULT_PASSWORD).await()
      }
      val user = checkNotNull(result.user) { "User auth failed" }
      try {
        user.getIdToken(true).await()
      } catch (_: Exception) {}
      kotlinx.coroutines.delay(300)
      user.uid
    }
  }

  protected fun Throwable.unwrapFirestoreException(): FirebaseFirestoreException? {
    var current: Throwable? = this
    while (current != null) {
      if (current is FirebaseFirestoreException) return current
      current = current.cause
    }
    return null
  }

  private companion object {
    const val EMULATOR_HOST = "127.0.0.1"
    const val FIRESTORE_PORT = 8085
    const val AUTH_PORT = 9099
    const val PROJECT_ID = "gen-lang-client-0970085058"
    const val DEFAULT_PASSWORD = "password123"
    const val AUTH_TIMEOUT_MS = 5000L
  }
}
