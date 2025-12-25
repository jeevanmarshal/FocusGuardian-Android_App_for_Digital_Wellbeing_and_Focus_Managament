package com.focusguardian

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.focusguardian.service.AppUsageService
import com.focusguardian.ui.DashboardScreen
import com.focusguardian.util.PermissionChecker

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Check critical permissions
        if (!PermissionChecker.hasUsageAccess(this)) {
            PermissionChecker.openUsageAccessSettings(this)
        }

        // Step 2: Start foreground monitoring service
        val serviceIntent = Intent(this, AppUsageService::class.java)
        startForegroundService(serviceIntent)

        // Step 3: Launch dashboard
        startActivity(Intent(this, DashboardScreen::class.java))
        finish()
    }
}
