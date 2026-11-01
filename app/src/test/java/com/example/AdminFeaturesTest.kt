package com.example

import com.example.data.MahfazaRepository
import com.example.data.local.ExpenseEntity
import com.example.data.local.IncomeEntity
import com.example.data.local.PackageConfigEntity
import com.example.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider
import android.content.Context
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class AdminFeaturesTest {

    private lateinit var repository: MahfazaRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        repository = MahfazaRepository(context)
    }

    @Test
    fun testExpenseManagement() = runBlocking {
        val expense = ExpenseEntity(UUID.randomUUID().toString(), "Test Expense", "General", 100.0, "TRY", "2024-01-01", "Desc", "admin1")
        repository.addExpense(expense)
        
        val expenses = repository.getAllExpenses().first()
        assertTrue(expenses.any { it.id == expense.id })
        
        repository.deleteExpense(expense.id)
        val expensesAfterDelete = repository.getAllExpenses().first()
        assertFalse(expensesAfterDelete.any { it.id == expense.id })
    }

    @Test
    fun testIncomeManagement() = runBlocking {
        val income = IncomeEntity(UUID.randomUUID().toString(), "user1", 200.0, "2024-01-01", "Test Income", "std_pro", "admin1")
        repository.addIncome(income)
        
        val incomes = repository.getAllIncomes().first()
        assertTrue(incomes.any { it.id == income.id })
        
        val totalIncome = repository.getTotalIncomeFlow().first()
        assertEquals(200.0, totalIncome ?: 0.0, 0.01)
        
        repository.deleteIncome(income)
        val totalIncomeAfterDelete = repository.getTotalIncomeFlow().first()
        assertEquals(0.0, totalIncomeAfterDelete ?: 0.0, 0.01)
    }

    @Test
    fun testPackageConfigManagement() = runBlocking {
        val config = PackageConfigEntity("pkg1", "Premium", "100", "1000", true, 50)
        repository.addPackageConfig(config)
        
        val configs = repository.getAllPackageConfigs().first()
        assertTrue(configs.any { it.id == "pkg1" })
        
        val updatedConfig = config.copy(price = "150")
        repository.updatePackageConfig(updatedConfig)
        
        val configsAfterUpdate = repository.getAllPackageConfigs().first()
        assertEquals("150", configsAfterUpdate.find { it.id == "pkg1" }?.price)
    }

    @Test
    fun testBackupAndRestore() = runBlocking {
        // Add some data
        repository.addExpense(ExpenseEntity("e1", "Exp", "Cat", 50.0, "TRY", "2024", "D", "a1"))
        repository.addIncome(IncomeEntity("i1", "u1", 500.0, "2024", "Inc", "p1", "a1"))
        
        val backupJson = repository.getBackupData()
        assertNotNull(backupJson)
        assertTrue(backupJson.contains("schemaVersion\":14"))

        
        // Clear and restore
        val success = repository.restoreDatabaseFromJson(backupJson)
        assertTrue(success)
        
        val restoredExpenses = repository.getAllExpenses().first()
        assertTrue(restoredExpenses.any { it.id == "e1" })
        
        val restoredIncomes = repository.getAllIncomes().first()
        assertTrue(restoredIncomes.any { it.id == "i1" })
    }

    @Test
    fun testAtomicUserDeletion() = runBlocking {
        val userId = "test_user"
        repository.register("Test User", "test@example.com", UserRole.STUDENT, EducationLevel.YKS_TYT_AYT, "std_free")
        val user = repository.currentUser.first()
        assertNotNull(user)
        val actualId = user!!.id
        
        repository.addTask(actualId, Task("t1", "Task", "Sub", "Top", 10, dayOfWeek = 1))
        
        val tasksBefore = repository.getTasks(actualId).first()
        assertEquals(1, tasksBefore.size)
        
        repository.deleteUserCompletely(actualId)
        
        val userAfter = repository.getUserById(actualId)
        assertNull(userAfter)
        
        val tasksAfter = repository.getTasks(actualId).first()
        assertEquals(0, tasksAfter.size)
    }
}
