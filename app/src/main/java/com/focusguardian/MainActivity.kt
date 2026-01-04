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
            val themeMode = com.focusguardian.util.UserPrefs.getThemeMode(this)
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            androidx.compose.material3.MaterialTheme(
                colorScheme = if (isDark) androidx.compose.material3.darkColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF6C63FF),
                    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC5),
                    background = androidx.compose.ui.graphics.Color(0xFF121212),
                    surface = androidx.compose.ui.graphics.Color(0xFF1E1E1E),
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color.White
                ) else androidx.compose.material3.lightColorScheme(
                    primary = androidx.compose.ui.graphics.Color(0xFF6200EE),
                    secondary = androidx.compose.ui.graphics.Color(0xFF03DAC5),
                    background = androidx.compose.ui.graphics.Color(0xFFFFFFFF),
                    surface = androidx.compose.ui.graphics.Color(0xFFF5F5F5),
                    onPrimary = androidx.compose.ui.graphics.Color.White,
                    onSurface = androidx.compose.ui.graphics.Color.Black
                )
            ) {
                 // Directly call proceedToApp when splash finishes. 
                 // No state switch to avoid blank screen.
                 SplashScreen {
                     proceedToApp()
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

        // Step 4: Launch dashboard
        startActivity(Intent(this, DashboardScreen::class.java))
        finish()
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
