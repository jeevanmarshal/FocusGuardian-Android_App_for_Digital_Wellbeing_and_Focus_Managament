package com.focusguardian

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.focusguardian.service.AppUsageService
import com.focusguardian.ui.DashboardScreen
import com.focusguardian.ui.SplashScreen
import com.focusguardian.util.PermissionChecker
import android.os.Build
import android.content.pm.PackageManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            com.focusguardian.ui.theme.FocusGuardianTheme(
                dynamicColor = false // Enforce our branded colors
            ) {
                 var showSplash by remember { mutableStateOf(true) }
                 
                 if (showSplash) {
                     SplashScreen {
                         proceedToApp()
                         showSplash = false
                     }
                 } else {
                     com.focusguardian.ui.MainContainer()
                 }
            }
        }
    }

    private fun proceedToApp() {
        // Step 1: Check critical usage access (Special permission)
        if (!PermissionChecker.hasUsageAccess(this)) {
            PermissionChecker.openUsageAccessSettings(this)
            // We advise user but continue to next check because they might go back
        }

        // Step 1.5: Check Overlay Permission (Required for alerts on Android 10+)
        if (!PermissionChecker.hasOverlayPermission(this)) {
            PermissionChecker.openOverlaySettings(this)
        }

        // Step 2: Request runtime permissions (Notifications, Phone State, Audio)
        requestNecessaryPermissions()

        // Step 3: Start foreground monitoring service
        val serviceIntent = Intent(this, AppUsageService::class.java)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Navigation is handled by State in setContent (switching to MainScreen)
    }

    private fun requestNecessaryPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        permissions.add(android.Manifest.permission.READ_PHONE_STATE)
        permissions.add(android.Manifest.permission.RECORD_AUDIO)

        val missing = permissions.filter { 
            checkSelfPermission(it) != android.content.pm.PackageManager.PERMISSION_GRANTED 
        }

        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 101)
        }
    }
}
