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
        const val NOTIFICATION_ID = 1001
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
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
}
