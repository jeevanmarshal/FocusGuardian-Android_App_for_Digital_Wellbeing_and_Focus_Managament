/*
package com.focusguardian.logic

import android.content.Context
// import com.focusguardian.util.AlertCooldown
import com.focusguardian.util.UserPrefs

/**
 * Handles progressive alert escalation (Gentle → Reminder → Strict)
 * for each monitored app session.
 */
object AlertModeHandler {

    // Usage time tracked per app (seconds)
    private val usageTimeMap = mutableMapOf<String, Int>()

    // Last triggered alert stage per app
    private val currentStageMap = mutableMapOf<String, AlertStage>()

    /* ==========================================================
       📱 APP IN FOREGROUND
       Called repeatedly while the app stays in foreground
       ========================================================== */

    fun onAppInForeground(context: Context, pkg: String) {

        // Global safety checks
        if (!UserPrefs.isAppMonitoringEnabled(context)) return
        if (UserPrefs.isEmergencyPauseActive(context)) return
        if (!UserPrefs.isAppMonitored(context, pkg)) return

        // Increase usage time (service calls every 2 seconds)
        val updatedSeconds = (usageTimeMap[pkg] ?: 0) + 2
        usageTimeMap[pkg] = updatedSeconds

        val minutesUsed = updatedSeconds / 60

        // User-defined thresholds
        val gentleX = UserPrefs.getGentleTime(context, pkg)
        val reminderY = UserPrefs.getReminderTime(context, pkg)
        val strictZ = UserPrefs.getStrictTime(context, pkg)

        // Decide alert stage
        val newStage = when {
            minutesUsed >= gentleX + reminderY + strictZ -> AlertStage.STRICT
            minutesUsed >= gentleX + reminderY -> AlertStage.REMINDER
            minutesUsed >= gentleX -> AlertStage.GENTLE
            else -> AlertStage.NONE
        }

        val lastStage = currentStageMap[pkg]

        // Fire alert only if:
        // 1. Stage is valid
        // 2. Stage changed
        // 3. Cooldown allows
        if (
            newStage != AlertStage.NONE &&
            newStage != lastStage &&
            AlertCooldown.canTrigger(pkg)
        ) {
            currentStageMap[pkg] = newStage
            AlertDispatcher.dispatch(context, newStage, pkg)
        }
    }

    /* ==========================================================
       🚪 APP EXIT
       Called when foreground app changes
       ========================================================== */

    fun onAppExit(pkg: String) {
        usageTimeMap.remove(pkg)
        currentStageMap.remove(pkg)
        AlertCooldown.reset(pkg)
    }
}
*/
package com.focusguardian.logic

object AlertModeHandler {

    fun onAppUsage(packageName: String, secondsUsed: Int) {
        // stub – logic will be added later
    }
}

