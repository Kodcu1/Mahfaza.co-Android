package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.data.MahfazaRepository
import com.example.data.local.ReminderSettingsEntity
import com.example.model.Task
import com.example.receiver.ReminderReceiver
import java.util.*
import kotlinx.coroutines.flow.firstOrNull

object ReminderManager {

    private const val DAILY_REMINDER_ID = 1001
    private const val SECOND_DAILY_REMINDER_ID = 1002
    private const val WEEKLY_REMINDER_ID = 1003
    private const val TASK_REMINDER_BASE_ID = 2000

    fun scheduleDailyReminder(context: Context, time: String, reminderId: Int = DAILY_REMINDER_ID) {
        val (hour, minute) = time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Mahfaza: Günlük Hatırlatıcı")
            putExtra("message", "Bugünkü çalışma hedeflerini kontrol et. Küçük bir adım, büyük bir ilerleme.")
            putExtra("notification_id", reminderId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    fun scheduleWeeklyReminder(context: Context, dayOfWeek: Int, time: String) {
        val (hour, minute) = time.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Mahfaza: Haftalık Planlama")
            putExtra("message", "Yeni haftanın programını hazırladın mı? Başarı planlı olmaktan geçer.")
            putExtra("notification_id", WEEKLY_REMINDER_ID)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WEEKLY_REMINDER_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }

    fun scheduleTaskReminder(context: Context, task: Task) {
        if (task.reminderTime == null) return
        
        val (hour, minute) = task.reminderTime.split(":").map { it.toInt() }
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, task.dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val reminderId = TASK_REMINDER_BASE_ID + task.id.hashCode()
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Mahfaza Hatırlatıcısı")
            putExtra("message", "'${task.title}' çalışman için zaman geldi!")
            putExtra("notification_id", reminderId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // For specific tasks, we might want exact alarms, but setInexact is safer for battery
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAll(context: Context) {
        val repository = MahfazaRepository(context)
        val user = repository.currentUser.value
        if (user == null) return

        val settings = repository.getReminderSettings(user.id).firstOrNull() ?: return
        
        if (!settings.notificationsEnabled) {
            cancelAll(context)
            return
        }

        if (settings.dailyReminderEnabled) {
            scheduleDailyReminder(context, settings.dailyReminderTime, DAILY_REMINDER_ID)
        } else {
            cancelReminder(context, DAILY_REMINDER_ID)
        }

        if (settings.secondDailyReminderEnabled) {
            scheduleDailyReminder(context, settings.secondDailyReminderTime, SECOND_DAILY_REMINDER_ID)
        } else {
            cancelReminder(context, SECOND_DAILY_REMINDER_ID)
        }

        if (settings.weeklyPlanReminderEnabled) {
            scheduleWeeklyReminder(context, settings.weeklyPlanReminderDay, settings.weeklyPlanReminderTime)
        } else {
            cancelReminder(context, WEEKLY_REMINDER_ID)
        }

        if (settings.taskRemindersEnabled) {
            val tasks = repository.getTasks(user.id).firstOrNull() ?: emptyList()
            tasks.filter { !it.isCompleted }.forEach { task ->
                scheduleTaskReminder(context, task)
            }
        }
    }

    fun cancelAll(context: Context) {
        cancelReminder(context, DAILY_REMINDER_ID)
        cancelReminder(context, SECOND_DAILY_REMINDER_ID)
        cancelReminder(context, WEEKLY_REMINDER_ID)
        // Task reminders are more complex to cancel all, but we can't easily iterate without the task list
        // In a real app we'd track active task reminder IDs in a DB or DataStore
    }
}
