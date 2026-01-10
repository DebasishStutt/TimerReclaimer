package com.timereclaim.app

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

/**
 * Helper class for creating and managing notifications
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "timereclaim_tracking_channel"
        const val REPORT_CHANNEL_ID = "timereclaim_report_channel"
        const val NOTIFICATION_ID = 1001
        const val REPORT_NOTIFICATION_ID = 1002
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Tracking Channel
            val trackingChannel = android.app.NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }

            // Report Channel
            val reportChannel = android.app.NotificationChannel(
                REPORT_CHANNEL_ID,
                context.getString(R.string.report_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.report_notification_channel_desc)
            }

            notificationManager.createNotificationChannel(trackingChannel)
            notificationManager.createNotificationChannel(reportChannel)
        }
    }
    
    /**
     * Create foreground service notification
     */
    fun createTrackingNotification(timeString: String, isLocked: Boolean): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val statusText = if (isLocked) {
            context.getString(R.string.notification_locked)
        } else {
            context.getString(R.string.notification_unlocked)
        }
        
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text, timeString))
            .setSubText(statusText)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true) // Makes it persistent
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setShowWhen(false)
            .setSound(null)
            .setVibrate(null)
            .build()
    }
    
    /**
     * Update notification with new time
     */
    fun updateNotification(timeString: String, isLocked: Boolean) {
        val notification = createTrackingNotification(timeString, isLocked)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Cancel notification
     */
    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Show notification for the daily report
     */
    fun showReportNotification(email: String, subject: String, body: String, dateString: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REPORT_NOTIFICATION_ID,
            Intent.createChooser(emailIntent, context.getString(R.string.main_email_label) ?: "Send Email"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, REPORT_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.report_notification_title))
            .setContentText(context.getString(R.string.report_notification_text, dateString))
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(REPORT_NOTIFICATION_ID, notification)
    }

    /**
     * Show a simple notification without pending intent
     */
    fun showSimpleNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(context, REPORT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(REPORT_NOTIFICATION_ID, notification)
    }
}
