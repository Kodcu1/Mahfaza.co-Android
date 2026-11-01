package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Mahfaza.co Hatırlatıcı"
        val message = intent.getStringExtra("message") ?: "Bugünkü hedeflerini kontrol etmeyi unutma!"
        val notificationId = intent.getIntExtra("notification_id", 1001)

        NotificationHelper.showSystemNotification(
            context = context,
            title = title,
            message = message,
            notificationId = notificationId,
            isReminder = true
        )
    }
}
