package com.focusguardian.domain.logic

import android.content.Context
import com.focusguardian.data.RuleStore
import com.focusguardian.util.UserPrefs

class RuleManager(
    private val context: Context,
    private val modeManager: ModeManager
) {

    enum class Decision {
        ALLOW,
        GENTLE_ALERT,
        REMINDER_ALERT,
        STRICT_BLOCK,
        BLOCK_FEED // For Shorts/Reels
    }

    enum class TargetType {
        APP, WEBSITE, SHORTS, REELS
    }

    fun evaluate(
        target: String,
        type: TargetType,
        usageSeconds: Long
    ): Decision {
        // 0. Bedtime & Mode Enforcement
        val currentMode = modeManager.getCurrentMode()
        
        if (UserPrefs.isBedtimeNow(context) || currentMode == ModeManager.ModeType.SLEEP) {
             if (UserPrefs.isAppMonitored(context, target)) {
                  return Decision.STRICT_BLOCK
             }
        }
        
        // Mode specific overrides (e.g. WORK mode strict on Social)
        if (currentMode == ModeManager.ModeType.WORK) {
             // Example: If it's a social app, lower the thresholds or go straight to REMINDER
             // This is a "mapping" of the WORK mode's intended logic.
        }

        val gentleLimit = UserPrefs.getGentleSeconds(context, target)
        val reminderLimit = UserPrefs.getReminderSeconds(context, target)
        val strictLimit = UserPrefs.getStrictSeconds(context, target)

        // Strict
        if (strictLimit > 0 && usageSeconds >= strictLimit) {
            val isStrictEnabled = UserPrefs.isStrictEnabled(context, target)
             if (isStrictEnabled) return Decision.STRICT_BLOCK
        }

        // Reminder
        if (reminderLimit > 0 && usageSeconds >= reminderLimit) {
             val isReminderEnabled = UserPrefs.isReminderEnabled(context, target)
             if (isReminderEnabled) return Decision.REMINDER_ALERT
        }

        // Gentle
        if (gentleLimit > 0 && usageSeconds >= gentleLimit) {
             val isGentleEnabled = UserPrefs.isGentleEnabled(context, target)
             if (isGentleEnabled) return Decision.GENTLE_ALERT
        }

        return Decision.ALLOW
    }
    
    // Helper to determine what to do for Shorts/Reels specific Logic
    fun evaluateFeedBlock(target: String): Decision {
        // If "Blocking Enforcement" is active for Shorts/Reels
        // This is a binary toggle in the Doc: "Alert Enforcement" OR "Blocking Enforcement"
        // storage for this specific config needs to be checked.
        
        // Assuming UserPrefs has a way to store this choice.
        // For now, returning ALLOW as placeholder until Prefs are fully verified.
        return Decision.ALLOW
    }
}
