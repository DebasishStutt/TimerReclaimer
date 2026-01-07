package com.timereclaim.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Main activity showing current tracking status
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var timeDisplay: TextView
    private lateinit var statusText: TextView
    private lateinit var sessionsCount: TextView
    private lateinit var emailDisplay: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        prefsManager = PreferencesManager.getInstance(this)
        
        // Initialize views
        timeDisplay = findViewById(R.id.timeDisplay)
        statusText = findViewById(R.id.statusText)
        sessionsCount = findViewById(R.id.sessionsCount)
        emailDisplay = findViewById(R.id.emailDisplay)
        
        // Set email
        emailDisplay.text = prefsManager.userEmail ?: "No email"
        
        // Ensure tracking service is running
        ensureTrackingServiceRunning()
        
        // Start UI update loop
        startUIUpdates()
    }
    
    /**
     * Ensure tracking service is running
     */
    private fun ensureTrackingServiceRunning() {
        val serviceIntent = Intent(this, TrackingService::class.java)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
    
    /**
     * Start periodic UI updates
     */
    private fun startUIUpdates() {
        lifecycleScope.launch {
            while (isActive) {
                updateUI()
                delay(1000) // Update every second
            }
        }
    }
    
    /**
     * Update UI with current data
     */
    private fun updateUI() {
        // Check if we need to reset for new day
        if (prefsManager.shouldResetForNewDay()) {
            prefsManager.resetDailyCounters()
        }
        
        // Update time display
        timeDisplay.text = prefsManager.getFormattedAccumulatedTime()
        
        // Update status
        statusText.text = if (prefsManager.isLocked) {
            getString(R.string.main_status_locked)
        } else {
            getString(R.string.main_status_unlocked)
        }
        
        // Update sessions count
        sessionsCount.text = prefsManager.lockSessionCount.toString()
    }
    
    override fun onResume() {
        super.onResume()
        updateUI()
    }
}
