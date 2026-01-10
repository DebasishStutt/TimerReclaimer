package com.timereclaim.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class EmailSettingsActivity : AppCompatActivity() {

    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_settings)

        prefsManager = PreferencesManager.getInstance(this)

        val etSenderEmail = findViewById<TextInputEditText>(R.id.etSenderEmail)
        val etAppPassword = findViewById<TextInputEditText>(R.id.etAppPassword)
        val btnSave = findViewById<MaterialButton>(R.id.btnSaveSettings)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        // Load existing values
        etSenderEmail.setText(prefsManager.senderEmail)
        etAppPassword.setText(prefsManager.senderAppPassword)

        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val email = etSenderEmail.text?.toString()?.trim()
            val password = etAppPassword.text?.toString()?.trim()

            if (email.isNullOrEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid Gmail address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isNullOrEmpty() || password.length < 16) {
                Toast.makeText(this, "Please enter the 16-character App Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Save to preferences
            prefsManager.senderEmail = email
            prefsManager.senderAppPassword = password

            Toast.makeText(this, getString(R.string.email_settings_saved), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
