package com.timereclaim.app

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

/**
 * Centralized preferences manager for storing and retrieving app data
 */
class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    companion object {
        private const val PREFS_NAME = "timereclaim_prefs"
        
        // Keys
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_ACCUMULATED_TIME = "accumulated_time_ms"
        private const val KEY_LAST_LOCK_TIME = "last_lock_time_ms"
        private const val KEY_IS_LOCKED = "is_locked"
        private const val KEY_LAST_RESET_DATE = "last_reset_date"
        private const val KEY_LOCK_SESSION_COUNT = "lock_session_count"
        private const val KEY_SENDER_EMAIL = "sender_email"
        private const val KEY_SENDER_APP_PASSWORD = "sender_app_password"
        
        @Volatile
        private var instance: PreferencesManager? = null
        
        fun getInstance(context: Context): PreferencesManager {
            return instance ?: synchronized(this) {
                instance ?: PreferencesManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // User Email
    var userEmail: String?
        get() = prefs.getString(KEY_USER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

    var senderEmail: String?
        get() = prefs.getString(KEY_SENDER_EMAIL, null)
        set(value) = prefs.edit().putString(KEY_SENDER_EMAIL, value).apply()

    var senderAppPassword: String?
        get() = prefs.getString(KEY_SENDER_APP_PASSWORD, null)
        set(value) = prefs.edit().putString(KEY_SENDER_APP_PASSWORD, value).apply()
    
    // Onboarding Status
    var isOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()
    
    // Accumulated Time (in milliseconds)
    var accumulatedTime: Long
        get() = prefs.getLong(KEY_ACCUMULATED_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_ACCUMULATED_TIME, value).apply()
    
    // Last Lock Time (timestamp in milliseconds)
    var lastLockTime: Long
        get() = prefs.getLong(KEY_LAST_LOCK_TIME, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_LOCK_TIME, value).apply()
    
    // Is Phone Currently Locked
    var isLocked: Boolean
        get() = prefs.getBoolean(KEY_IS_LOCKED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_LOCKED, value).apply()
    
    // Last Reset Date
    var lastResetDate: String?
        get() = prefs.getString(KEY_LAST_RESET_DATE, null)
        set(value) = prefs.edit().putString(KEY_LAST_RESET_DATE, value).apply()
    
    // Lock Session Count
    var lockSessionCount: Int
        get() = prefs.getInt(KEY_LOCK_SESSION_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_LOCK_SESSION_COUNT, value).apply()
    
    /**
     * Reset daily counters
     */
    fun resetDailyCounters() {
        val today = getCurrentDate()
        prefs.edit().apply {
            putLong(KEY_ACCUMULATED_TIME, 0L)
            putLong(KEY_LAST_LOCK_TIME, 0L)
            putBoolean(KEY_IS_LOCKED, false)
            putString(KEY_LAST_RESET_DATE, today)
            putInt(KEY_LOCK_SESSION_COUNT, 0)
            apply()
        }
    }
    
    /**
     * Check if we need to reset for a new day
     */
    fun shouldResetForNewDay(): Boolean {
        val lastReset = lastResetDate ?: return true
        val today = getCurrentDate()
        return lastReset != today
    }
    
    /**
     * Get current date in YYYY-MM-DD format
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * Format accumulated time to HH:MM:SS
     */
    fun getFormattedAccumulatedTime(): String {
        val totalSeconds = accumulatedTime / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    /**
     * Increment lock session count
     */
    fun incrementSessionCount() {
        lockSessionCount += 1
    }

    /**
     * Get report date string
     */
    fun getReportDate(): String {
        val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }

    /**
     * Get report subject
     */
    fun getReportSubject(context: Context): String {
        return context.getString(R.string.email_subject, getReportDate())
    }

    /**
     * Get report body
     */
    fun getReportBody(context: Context): String {
        return context.getString(
            R.string.email_body,
            getReportDate(),
            getFormattedAccumulatedTime(),
            lockSessionCount
        )
    }
}
