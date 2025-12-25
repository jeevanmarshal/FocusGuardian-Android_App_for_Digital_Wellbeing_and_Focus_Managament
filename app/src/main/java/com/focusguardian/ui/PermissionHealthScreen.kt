package com.focusguardian.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.focusguardian.util.PermissionChecker

class PermissionHealthScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Permission Health"
            textSize = 20f
        }

        /* ---------- Usage Access ---------- */

        val usageStatus = TextView(this).apply {
            text = if (PermissionChecker.hasUsageAccess(this@PermissionHealthScreen))
                "Usage Access: Enabled ✅"
            else
                "Usage Access: Disabled ❌"
        }

        val usageFix = Button(this).apply {
            text = "Fix Usage Access"
            setOnClickListener {
                PermissionChecker.openUsageAccessSettings(this@PermissionHealthScreen)
            }
        }

        /* ---------- Notification ---------- */

        val notifStatus = TextView(this).apply {
            text = if (PermissionChecker.hasNotificationPermission(this@PermissionHealthScreen))
                "Notifications: Enabled ✅"
            else
                "Notifications: Disabled ❌"
        }

        val notifFix = Button(this).apply {
            text = "Fix Notification Permission"
            setOnClickListener {
                PermissionChecker.openNotificationSettings(this@PermissionHealthScreen)
            }
        }

        /* ---------- Battery Optimization ---------- */

        val batteryStatus = TextView(this).apply {
            text = if (PermissionChecker.isIgnoringBatteryOptimizations(this@PermissionHealthScreen))
                "Battery Optimization: Ignored ✅"
            else
                "Battery Optimization: Active ❌"
        }

        val batteryFix = Button(this).apply {
            text = "Disable Battery Optimization"
            setOnClickListener {
                PermissionChecker.openBatteryOptimizationSettings(this@PermissionHealthScreen)
            }
        }

        layout.addView(title)
        layout.addView(usageStatus)
        layout.addView(usageFix)
        layout.addView(notifStatus)
        layout.addView(notifFix)
        layout.addView(batteryStatus)
        layout.addView(batteryFix)

        setContentView(layout)
    }
}
