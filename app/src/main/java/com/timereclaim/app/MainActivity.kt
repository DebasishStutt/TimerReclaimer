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
        
        // Setup send report button
        findViewById<android.view.View>(R.id.btnSendReport).setOnClickListener {
            sendOnDemandReport()
        }

        // Setup menu button
        findViewById<android.widget.ImageButton>(R.id.btnMenu).setOnClickListener {
            showPopupMenu(it)
        }
        
        // Ensure tracking service is running
        ensureTrackingServiceRunning()
        
        // Start UI update loop
        startUIUpdates()
    }

    /**
     * Show popup menu
     */
    private fun showPopupMenu(view: android.view.View) {
        val popup = android.widget.PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_main, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                    true
                }
                R.id.action_email_settings -> {
                    startActivity(Intent(this, EmailSettingsActivity::class.java))
                    true
                }
                R.id.action_developer -> {
                    startActivity(Intent(this, DeveloperActivity::class.java))
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    /**
     * Send email report on demand
     */
    private fun sendOnDemandReport() {
        val email = prefsManager.userEmail ?: return
        val subject = prefsManager.getReportSubject(this)
        val body = prefsManager.getReportBody(this)
        val senderEmail = prefsManager.senderEmail
        val appPassword = prefsManager.senderAppPassword

        // If automated credentials exist, use them
        if (!senderEmail.isNullOrEmpty() && !appPassword.isNullOrEmpty()) {
            lifecycleScope.launch {
                val progressToast = android.widget.Toast.makeText(this@MainActivity, "Sending report...", android.widget.Toast.LENGTH_SHORT)
                progressToast.show()
                
                val success = EmailSender.sendEmail(senderEmail, appPassword, email, subject, body)
                
                if (success) {
                    android.widget.Toast.makeText(this@MainActivity, getString(R.string.report_sent_success), android.widget.Toast.LENGTH_LONG).show()
                } else {
                    // Fallback to manual intent
                    launchManualEmailIntent(email, subject, body)
                }
            }
        } else {
            // No credentials, use manual intent
            launchManualEmailIntent(email, subject, body)
        }
    }

    private fun launchManualEmailIntent(email: String, subject: String, body: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = android.net.Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        
        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.main_email_label)))
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to launch email app", e)
            android.widget.Toast.makeText(
                this,
                getString(R.string.email_no_app_error),
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
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
