package com.timereclaim.app

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*

/**
 * Foreground service that tracks phone lock time
 */
class TrackingService : LifecycleService() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var screenStateReceiver: ScreenStateReceiver
    
    private var updateJob: Job? = null
    
    companion object {
        private const val TAG = "TrackingService"
        const val ACTION_START_TRACKING = "com.timereclaim.app.START_TRACKING"
        const val ACTION_STOP_TRACKING = "com.timereclaim.app.STOP_TRACKING"
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        prefsManager = PreferencesManager.getInstance(this)
        notificationHelper = NotificationHelper(this)
        
        // Register screen state receiver
        screenStateReceiver = ScreenStateReceiver()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        registerReceiver(screenStateReceiver, filter)
        
        // Start as foreground service
        startForeground(NotificationHelper.NOTIFICATION_ID, 
            notificationHelper.createTrackingNotification(
                prefsManager.getFormattedAccumulatedTime(),
                prefsManager.isLocked
            )
        )
        
        // Check if we need to reset for a new day
        if (prefsManager.shouldResetForNewDay()) {
            prefsManager.resetDailyCounters()
        }
        
        // Start periodic update
        startPeriodicUpdate()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d(TAG, "Service started with action: ${intent?.action}")
        
        when (intent?.action) {
            ACTION_STOP_TRACKING -> {
                stopSelf()
            }
        }
        
        return START_STICKY // Restart service if killed by system
    }
    
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }
    
    /**
     * Start periodic update of notification and accumulated time
     */
    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = lifecycleScope.launch {
            while (isActive) {
                updateAccumulatedTime()
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Update accumulated time if phone is locked
     */
    private fun updateAccumulatedTime() {
        if (prefsManager.isLocked && prefsManager.lastLockTime > 0) {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastUpdate = currentTime - prefsManager.lastLockTime
            
            // Update accumulated time
            prefsManager.accumulatedTime += timeSinceLastUpdate
            prefsManager.lastLockTime = currentTime
            
            // Update notification
            notificationHelper.updateNotification(
                prefsManager.getFormattedAccumulatedTime(),
                true
            )
        }
    }
    
    /**
     * Handle screen lock event
     */
    fun onScreenLocked() {
        Log.d(TAG, "Screen locked")
        prefsManager.isLocked = true
        prefsManager.lastLockTime = System.currentTimeMillis()
        prefsManager.incrementSessionCount()
        
        // Update notification immediately
        notificationHelper.updateNotification(
            prefsManager.getFormattedAccumulatedTime(),
            true
        )
    }
    
    /**
     * Handle screen unlock event
     */
    fun onScreenUnlocked() {
        Log.d(TAG, "Screen unlocked")
        
        // Save accumulated time before pausing
        if (prefsManager.isLocked && prefsManager.lastLockTime > 0) {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastUpdate = currentTime - prefsManager.lastLockTime
            prefsManager.accumulatedTime += timeSinceLastUpdate
        }
        
        prefsManager.isLocked = false
        prefsManager.lastLockTime = 0
        
        // Update notification
        notificationHelper.updateNotification(
            prefsManager.getFormattedAccumulatedTime(),
            false
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        
        // Save final state
        if (prefsManager.isLocked && prefsManager.lastLockTime > 0) {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastUpdate = currentTime - prefsManager.lastLockTime
            prefsManager.accumulatedTime += timeSinceLastUpdate
        }
        
        updateJob?.cancel()
        
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
        
        notificationHelper.cancelNotification()
    }
}
