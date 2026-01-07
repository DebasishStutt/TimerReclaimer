package com.timereclaim.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Broadcast receiver for screen state changes
 */
class ScreenStateReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "ScreenStateReceiver"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        Log.d(TAG, "Received action: ${intent.action}")
        
        // Find the running tracking service
        val serviceIntent = Intent(context, TrackingService::class.java)
        
        when (intent.action) {
            Intent.ACTION_SCREEN_OFF -> {
                // Screen is locked
                Log.d(TAG, "Screen OFF detected")
                
                // Notify service through preferences
                val prefs = PreferencesManager.getInstance(context)
                prefs.isLocked = true
                prefs.lastLockTime = System.currentTimeMillis()
                prefs.incrementSessionCount()
                
                // Service will detect the change and update
            }
            
            Intent.ACTION_USER_PRESENT -> {
                // Screen is unlocked (user has passed lock screen)
                Log.d(TAG, "User PRESENT detected (unlocked)")
                
                // Notify service through preferences
                val prefs = PreferencesManager.getInstance(context)
                
                // Save accumulated time
                if (prefs.isLocked && prefs.lastLockTime > 0) {
                    val currentTime = System.currentTimeMillis()
                    val timeSinceLastUpdate = currentTime - prefs.lastLockTime
                    prefs.accumulatedTime += timeSinceLastUpdate
                }
                
                prefs.isLocked = false
                prefs.lastLockTime = 0
            }
        }
    }
}
