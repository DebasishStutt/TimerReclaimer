package com.timereclaim.app

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        val versionDisplay = findViewById<TextView>(R.id.versionDisplay)
        
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            versionDisplay.text = version
        } catch (e: Exception) {
            versionDisplay.text = "1.0.0"
        }
    }
}
