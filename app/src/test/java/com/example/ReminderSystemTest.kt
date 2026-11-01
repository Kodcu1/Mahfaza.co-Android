package com.example

import com.example.data.local.ReminderSettingsEntity
import com.example.model.Task
import com.example.model.TaskPriority
import org.junit.Assert.*
import org.junit.Test

class ReminderSystemTest {

    @Test
    fun testReminderSettingsDefaults() {
        val defaultSettings = ReminderSettingsEntity(userId = "student-123")
        
        assertEquals("student-123", defaultSettings.userId)
        assertTrue(defaultSettings.notificationsEnabled)
        assertTrue(defaultSettings.dailyReminderEnabled)
        assertEquals("18:00", defaultSettings.dailyReminderTime)
        assertFalse(defaultSettings.secondDailyReminderEnabled)
        assertEquals("21:00", defaultSettings.secondDailyReminderTime)
        assertTrue(defaultSettings.taskRemindersEnabled)
        assertTrue(defaultSettings.incompleteTaskReminderEnabled)
        assertTrue(defaultSettings.weeklyPlanReminderEnabled)
        assertEquals(1, defaultSettings.weeklyPlanReminderDay) // Sunday
        assertEquals("18:00", defaultSettings.weeklyPlanReminderTime)
    }

    @Test
    fun testReminderSettingsCustomization() {
        val customSettings = ReminderSettingsEntity(
            userId = "student-456",
            notificationsEnabled = true,
            dailyReminderEnabled = true,
            dailyReminderTime = "09:30",
            secondDailyReminderEnabled = true,
            secondDailyReminderTime = "22:00",
            taskRemindersEnabled = false,
            incompleteTaskReminderEnabled = true,
            weeklyPlanReminderEnabled = true,
            weeklyPlanReminderDay = 2, // Monday
            weeklyPlanReminderTime = "20:00"
        )

        assertEquals("09:30", customSettings.dailyReminderTime)
        assertEquals("22:00", customSettings.secondDailyReminderTime)
        assertTrue(customSettings.secondDailyReminderEnabled)
        assertFalse(customSettings.taskRemindersEnabled)
        assertEquals(2, customSettings.weeklyPlanReminderDay)
        assertEquals("20:00", customSettings.weeklyPlanReminderTime)
    }

    @Test
    fun testTaskReminderField() {
        val taskWithReminder = Task(
            id = "task-1",
            title = "Matematik - Fonksiyonlar",
            subject = "Matematik",
            topic = "Fonksiyonlar",
            targetQuestionCount = 40,
            completedQuestionCount = 20,
            isCompleted = false,
            priority = TaskPriority.HIGH,
            xpReward = 20,
            dayOfWeek = 3,
            reminderTime = "14:30"
        )

        assertNotNull(taskWithReminder.reminderTime)
        assertEquals("14:30", taskWithReminder.reminderTime)
        assertFalse(taskWithReminder.isCompleted)
    }

    @Test
    fun testReminderTimeFormatValidation() {
        val validTime = "18:00"
        val parts = validTime.split(":")
        assertEquals(2, parts.size)
        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        assertNotNull(hour)
        assertNotNull(minute)
        assertTrue(hour!! in 0..23)
        assertTrue(minute!! in 0..59)
    }
}
