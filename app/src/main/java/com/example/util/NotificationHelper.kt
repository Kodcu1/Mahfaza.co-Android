package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID = "mahfaza_notifications_channel"
    private const val CHANNEL_NAME = "Mahfaza.co Bildirimleri"
    private const val CHANNEL_DESC = "Görev, çalışma onayı, mesaj ve seviye bildirimleri"

    private const val REMINDER_CHANNEL_ID = "mahfaza_study_reminders"
    private const val REMINDER_CHANNEL_NAME = "Çalışma Hatırlatıcıları"
    private const val REMINDER_CHANNEL_DESC = "Günlük çalışma ve görev hatırlatmaları"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // General channel
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = CHANNEL_DESC
            }
            notificationManager.createNotificationChannel(channel)

            // Reminder channel
            val reminderChannel = NotificationChannel(REMINDER_CHANNEL_ID, REMINDER_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
                description = REMINDER_CHANNEL_DESC
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(reminderChannel)
        }
    }

    fun showSystemNotification(
        context: Context, 
        title: String, 
        message: String, 
        notificationId: Int = System.currentTimeMillis().toInt(),
        isReminder: Boolean = false
    ) {
        try {
            createNotificationChannels(context)
            
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val channelId = if (isReminder) REMINDER_CHANNEL_ID else CHANNEL_ID
            
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(if (isReminder) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))

            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
