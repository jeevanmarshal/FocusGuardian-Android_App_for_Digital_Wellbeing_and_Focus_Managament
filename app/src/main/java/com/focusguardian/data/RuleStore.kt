package com.focusguardian.data

import android.content.Context
import com.focusguardian.util.UserPrefs

object RuleStore {

    fun saveAppRules(
        context: Context,
        pkgs: List<String>,
        gentleSeconds: Long,
        reminderSeconds: Long,
        strictSeconds: Long,
        gentleMsg: String,
        reminderMsg: String,
        strictMsg: String,
        pendingTask: String,
        isGentleEnabled: Boolean,
        isReminderEnabled: Boolean,
        isStrictEnabled: Boolean,
        isEmergencyAllowed: Boolean,
        pauseDurationMinutes: Int,
        appIntent: String,
        isAIAdaptive: Boolean,
        sensitivity: Int
    ) {
        pkgs.forEach { pkg ->
            // Time Thresholds
            UserPrefs.setGentleSeconds(context, pkg, gentleSeconds)
            UserPrefs.setReminderSeconds(context, pkg, reminderSeconds)
            UserPrefs.setStrictSeconds(context, pkg, strictSeconds)

            // Messages
            UserPrefs.setGentleMessage(context, pkg, gentleMsg)
            UserPrefs.setReminderMessage(context, pkg, reminderMsg)
            UserPrefs.setStrictMessage(context, pkg, strictMsg)
            UserPrefs.setPendingTask(context, pkg, pendingTask)

            // Enforcement Toggles
            UserPrefs.setGentleEnabled(context, pkg, isGentleEnabled)
            UserPrefs.setReminderEnabled(context, pkg, isReminderEnabled)
            UserPrefs.setStrictEnabled(context, pkg, isStrictEnabled)
            UserPrefs.setEmergencyUnlockAllowed(context, pkg, isEmergencyAllowed)
            
            // Allow Strict Duration (Pause Duration)
            UserPrefs.setPauseDurationMinutes(context, pkg, pauseDurationMinutes)

            // AI & Intent
            UserPrefs.setAppIntent(context, pkg, appIntent)
            UserPrefs.setAIAdaptiveEnabled(context, pkg, isAIAdaptive)
            UserPrefs.setCognitiveSensitivity(context, pkg, sensitivity)
        }
    }

    fun resetAppBlock(context: Context, pkgs: List<String>) {
        pkgs.forEach { pkg ->
            UserPrefs.setAppPausedUntil(context, pkg, 0)
        }
    }
}
