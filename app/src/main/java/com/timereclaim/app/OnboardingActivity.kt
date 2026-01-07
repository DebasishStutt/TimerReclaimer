package com.timereclaim.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Onboarding activity for first-time setup
 */
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var prefsManager: PreferencesManager
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var emailInput: TextInputEditText
    private lateinit var getStartedButton: MaterialButton
    
    // Permission launcher for notifications
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            requestBatteryOptimization()
        } else {
            Toast.makeText(
                this,
                getString(R.string.permission_notification_rationale),
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager.getInstance(this)
        
        // If onboarding is already complete, go to main activity
        if (prefsManager.isOnboardingComplete) {
            navigateToMain()
            return
        }
        
        setContentView(R.layout.activity_onboarding)
        
        // Initialize views
        emailInputLayout = findViewById(R.id.emailInputLayout)
        emailInput = findViewById(R.id.emailInput)
        getStartedButton = findViewById(R.id.getStartedButton)
        
        // Set up button click
        getStartedButton.setOnClickListener {
            validateAndProceed()
        }
    }
    
    /**
     * Validate email and proceed with permissions
     */
    private fun validateAndProceed() {
        val email = emailInput.text?.toString()?.trim()
        
        if (email.isNullOrEmpty()) {
            emailInputLayout.error = getString(R.string.onboarding_email_error)
            return
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.error = getString(R.string.onboarding_email_error)
            return
        }
        
        // Clear error
        emailInputLayout.error = null
        
        // Save email
        prefsManager.userEmail = email
        
        // Request notification permission
        requestNotificationPermission()
    }
    
    /**
     * Request notification permission (Android 13+)
     */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    requestBatteryOptimization()
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // No need to request on Android 12 and below
            requestBatteryOptimization()
        }
    }
    
    /**
     * Request battery optimization exemption
     */
    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            val packageName = packageName
            
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Fallback if the specific action is not available
                    Toast.makeText(
                        this,
                        getString(R.string.permission_battery_rationale),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        // Complete onboarding
        completeOnboarding()
    }
    
    /**
     * Complete onboarding and navigate to main activity
     */
    private fun completeOnboarding() {
        // Mark onboarding as complete
        prefsManager.isOnboardingComplete = true
        
        // Schedule daily alarm
        DailyResetReceiver.scheduleDailyAlarm(this)
        
        // Start tracking service
        val serviceIntent = Intent(this, TrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        
        // Navigate to main
        navigateToMain()
    }
    
    /**
     * Navigate to main activity
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        finish()
    }
}
