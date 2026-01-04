
package com.focusguardian.logic

import android.content.Context
import com.focusguardian.util.AlertCooldown
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

        // 1. CHECK BLOCKING (Strict Pause)
        if (UserPrefs.isAppPaused(context, pkg)) {
            // App is currently strictly blocked. Do not track time.
            // Dispatch BLOCKED alert immediately to force overlay/exit.
            // We use a special stage or just re-dispatch STRICT with a "Blocked" flag?
            // Let's use a dedicated mechanism or AlertStage.STRICT with different message?
            // User requested "Correct the logics".
            // If I just return, UI stays open.
            // I must dispatch.
            AlertDispatcher.dispatch(context, AlertStage.BLOCKED, pkg)
            return
        }

        // Track analytics
        val category = com.focusguardian.logic.AppCategoryResolver.resolve(context, pkg)
        com.focusguardian.util.AnalyticsStore.addCategoryUsage(context, category.name, 2)
        // ... (analytics code same as before, simplified for strictness)
        if (UserPrefs.isAppMonitored(context, pkg)) {
             com.focusguardian.util.AnalyticsStore.addDistractedTime(context, 2)
        } else {
             com.focusguardian.util.AnalyticsStore.addFocusedTime(context, 2)
        }

        if (!UserPrefs.isAppMonitored(context, pkg)) return

        // Increase usage time (service calls every 2 seconds)
        val updatedSeconds = (usageTimeMap[pkg] ?: 0) + 2
        usageTimeMap[pkg] = updatedSeconds

        // User-defined thresholds (Seconds) - Force minimum 10s to prevent immediate stacking
        var gentleX = UserPrefs.getGentleSeconds(context, pkg).coerceAtLeast(10L)
        var reminderY = UserPrefs.getReminderSeconds(context, pkg).coerceAtLeast(10L)
        var strictZ = UserPrefs.getStrictSeconds(context, pkg).coerceAtLeast(10L)

        // AI Adaptive adjustment (Copy existing logic)
        if (UserPrefs.isAIAdaptiveEnabled(context, pkg)) {
            val mood = com.focusguardian.logic.CognitiveAnalyzer.getCognitiveLoad(context)
            val globalIntent = UserPrefs.getGlobalIntent(context)
            val appIntent = UserPrefs.getAppIntent(context, pkg)
            val isMismatch = (globalIntent == "STUDY" || globalIntent == "WORK") && 
                             (appIntent == "SOCIAL" || appIntent == "RELAX")

            when (mood) {
                com.focusguardian.logic.CognitiveState.STRESSED -> {
                    val factor = if (isMismatch) 0.6f else 0.8f
                    gentleX = (gentleX * factor).toLong().coerceAtLeast(10)
                    reminderY = (reminderY * factor).toLong().coerceAtLeast(10)
                }
                com.focusguardian.logic.CognitiveState.DISTRACTED -> {
                    if (isMismatch) {
                        gentleX = (gentleX * 0.7f).toLong().coerceAtLeast(10)
                    }
                }
                com.focusguardian.logic.CognitiveState.CALM -> {
                    gentleX = (gentleX * 1.2).toLong()
                }
                else -> {}
            }
        }

        // Decide alert stage
        val newStage = when {
            updatedSeconds >= gentleX + reminderY + strictZ -> AlertStage.STRICT
            updatedSeconds >= gentleX + reminderY -> AlertStage.REMINDER
            updatedSeconds >= gentleX -> AlertStage.GENTLE
            else -> AlertStage.NONE
        }

        val lastStage = currentStageMap[pkg]

        val isEnabled = when (newStage) {
            AlertStage.GENTLE -> UserPrefs.isGentleEnabled(context, pkg)
            AlertStage.REMINDER -> UserPrefs.isReminderEnabled(context, pkg)
            AlertStage.STRICT -> true // Strict Alert always triggers if time is reached, enforcement toggle is for Blocking
            else -> false
        }

        if (
            newStage != AlertStage.NONE &&
            newStage != lastStage &&
            isEnabled
        ) {
            currentStageMap[pkg] = newStage
            AlertDispatcher.dispatch(context, newStage, pkg)
        }
    }

    /* ==========================================================
       🚪 APP EXIT
       Called when foreground app changes
       ========================================================== */

    fun onAppExit(context: android.content.Context, pkg: String) {
        val lastStage = currentStageMap[pkg] ?: AlertStage.NONE
        
        // If user was alerted but successfully exited before strict block, reward them
        if (lastStage == AlertStage.GENTLE || lastStage == AlertStage.REMINDER) {
            val intent = android.content.Intent(context, com.focusguardian.ui.PositiveFeedbackScreen::class.java)
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
        
        // If exiting from STRICT mode, enforce the block (Pause)
        if (lastStage == AlertStage.STRICT && UserPrefs.isStrictEnabled(context, pkg)) {
             val pauseDuration = UserPrefs.getPauseDurationMinutes(context, pkg)
             if (pauseDuration > 0) {
                 val pauseUntil = System.currentTimeMillis() + (pauseDuration * 60 * 1000L)
                 UserPrefs.setAppPausedUntil(context, pkg, pauseUntil)
             }
        }

        usageTimeMap.remove(pkg)
        currentStageMap.remove(pkg)
        AlertCooldown.reset(pkg)
    }

    fun resetAll() {
        usageTimeMap.clear()
        currentStageMap.clear()
    }
}

