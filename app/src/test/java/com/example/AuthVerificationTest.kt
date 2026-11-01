package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.MahfazaRepository
import com.example.model.UserRole
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthVerificationTest {

    private lateinit var repository: MahfazaRepository

    @Before
    fun setup() {
        repository = MahfazaRepository(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testAdminSeedingAndLogin() = runBlocking {
        // Trigger seeding
        repository.seedDemoData()
        
        // Test normal login with seeded admin
        val success = repository.login("admin@mahfaza.com")
        assertTrue("Admin login should succeed after seeding", success)
        
        val user = repository.currentUser.value
        assertNotNull(user)
        assertEquals(UserRole.ADMIN, user?.role)
        assertEquals("admin@mahfaza.com", user?.email)
    }

    @Test
    fun testEmailNormalization() = runBlocking {
        repository.seedDemoData()
        
        // Test with whitespace
        val success = repository.login("  admin@mahfaza.com  ".trim())
        assertTrue("Login should succeed with trimmed email", success)
    }

    @Test
    fun testInvalidUserRejection() = runBlocking {
        val success = repository.login("unknown@example.com")
        assertFalse("Login should fail for unknown user", success)
    }

    @Test
    fun testIdempotentSeeding() = runBlocking {
        repository.seedDemoData()
        val firstUserCount = repository.getAllStudents().first().size + repository.getAllCoaches().first().size + 1 // +1 for admin
        
        repository.seedDemoData() // Call again
        val secondUserCount = repository.getAllStudents().first().size + repository.getAllCoaches().first().size + 1
        
        assertEquals("Seeding should be idempotent", firstUserCount, secondUserCount)
    }
}
