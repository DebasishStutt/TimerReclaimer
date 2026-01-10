package com.timereclaim.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

/**
 * Receiver for daily reset and email report at 23:59
 */
class DailyResetReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "DailyResetReceiver"
        const val ACTION_DAILY_RESET = "com.timereclaim.app.DAILY_RESET"
        
        /**
         * Schedule daily alarm at 23:59
         */
        fun scheduleDailyAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, DailyResetReceiver::class.java).apply {
                action = ACTION_DAILY_RESET
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Calculate 23:59 today
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If it's already past 23:59, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            
            Log.d(TAG, "Scheduling alarm for: ${calendar.time}")
            
            // Schedule exact alarm at 23:59
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        if (intent.action == ACTION_DAILY_RESET) {
            Log.d(TAG, "Daily reset triggered")
            
            val prefs = PreferencesManager.getInstance(context)
            val email = prefs.userEmail
            
            if (email != null) {
                sendDailyReport(context, email, prefs)
            }
            
            // Reset counters for new day
            prefs.resetDailyCounters()
            
            // Schedule next alarm
            scheduleDailyAlarm(context)
        }
    }
    
    /**
     * Send daily report
     */
    private fun sendDailyReport(context: Context, email: String, prefs: PreferencesManager) {
        val subject = prefs.getReportSubject(context)
        val body = prefs.getReportBody(context)
        val date = prefs.getReportDate()
        val senderEmail = prefs.senderEmail
        val appPassword = prefs.senderAppPassword

        val notificationHelper = NotificationHelper(context)

        // Try automated sending if credentials are set
        if (!senderEmail.isNullOrEmpty() && !appPassword.isNullOrEmpty()) {
            // Use GlobalScope for this short background task in receiver
            @Suppress("OPT_IN_USAGE")
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                val success = EmailSender.sendEmail(
                    senderEmail,
                    appPassword,
                    email,
                    subject,
                    body
                )

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (success) {
                        notificationHelper.showSimpleNotification(
                            context.getString(R.string.report_sent_success),
                            context.getString(R.string.report_notification_text, date)
                        )
                    } else {
                        // Fallback to manual intent notification if SMTP fails
                        notificationHelper.showReportNotification(email, subject, body, date)
                    }
                }
            }
        } else {
            // No credentials, use manual intent notification
            notificationHelper.showReportNotification(email, subject, body, date)
        }
    }
}
