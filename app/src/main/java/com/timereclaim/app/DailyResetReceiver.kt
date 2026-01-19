package com.timereclaim.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
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
            
            // Check for exact alarm permission on Android 12+
            val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

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
            
            Log.d(TAG, "Scheduling alarm for: ${calendar.time} (Exact: $canScheduleExact)")
            
            try {
                if (canScheduleExact) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback to inexact alarm if permission is missing
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "SecurityException while scheduling exact alarm", e)
                // Fallback to inexact
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        if (intent.action == ACTION_DAILY_RESET) {
            Log.d(TAG, "Daily reset triggered")
            
            // Reschedule for tomorrow
            scheduleDailyAlarm(context)
            
            val prefs = PreferencesManager.getInstance(context)
            val email = prefs.userEmail
            
            if (email != null) {
                // IMPORTANT: Capture report data BEFORE resetting counters
                val subject = prefs.getReportSubject(context)
                val body = prefs.getReportBody(context)
                val date = prefs.getReportDate()

                // Use goAsync to allow background work to complete
                val pendingResult = goAsync()
                GlobalScope.launch {
                    try {
                        sendDailyReportInternal(context, email, subject, body, date, prefs)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            
            // Reset counters for new day
            prefs.resetDailyCounters()
        }
    }
    
    /**
     * Internal method to send the captured report
     */
    private suspend fun sendDailyReportInternal(
        context: Context,
        email: String,
        subject: String,
        body: String,
        date: String,
        prefs: PreferencesManager
    ) {
        val senderEmail = prefs.senderEmail
        val appPassword = prefs.senderAppPassword
        val notificationHelper = NotificationHelper(context)

        // Try automated sending if credentials are set
        if (!senderEmail.isNullOrEmpty() && !appPassword.isNullOrEmpty()) {
            val success = EmailSender.sendEmail(
                senderEmail,
                appPassword,
                email,
                subject,
                body
            )

            withContext(Dispatchers.Main) {
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
        } else {
            // No credentials, use manual intent notification
            withContext(Dispatchers.Main) {
                notificationHelper.showReportNotification(email, subject, body, date)
            }
        }
    }
}
