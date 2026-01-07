package com.timereclaim.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
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
     * Send daily report via email intent
     */
    private fun sendDailyReport(context: Context, email: String, prefs: PreferencesManager) {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())
        
        val formattedTime = prefs.getFormattedAccumulatedTime()
        val sessionCount = prefs.lockSessionCount
        
        val subject = context.getString(R.string.email_subject, today)
        val body = context.getString(R.string.email_body, today, formattedTime, sessionCount)
        
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            context.startActivity(emailIntent)
            Log.d(TAG, "Email intent launched successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch email app", e)
            Toast.makeText(
                context,
                context.getString(R.string.email_no_app_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
