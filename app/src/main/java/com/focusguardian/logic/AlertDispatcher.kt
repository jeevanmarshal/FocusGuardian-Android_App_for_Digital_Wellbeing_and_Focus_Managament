package com.focusguardian.logic

import android.content.Context
import android.content.Intent
import com.focusguardian.service.VoiceAlertService
import com.focusguardian.ui.FocusAlertActivity
import com.focusguardian.util.UserPrefs

object AlertDispatcher {

    fun dispatch(context: Context, stage: AlertStage, pkg: String) {
        val appName = UserPrefs.getAppName(context, pkg)
        
        // Construct Message: "You are reached the time limit" of App Name + User-Defined Message + Pending Task
        val baseMsg = "You are reached the time limit of $appName."
        val userMsg = when (stage) {
            AlertStage.GENTLE -> UserPrefs.getGentleMessage(context, pkg)
            AlertStage.REMINDER -> UserPrefs.getReminderMessage(context, pkg)
            AlertStage.STRICT -> UserPrefs.getStrictMessage(context, pkg)
            else -> ""
        }
        
        var fullMessage = "$baseMsg $userMsg"
        
        if (stage == AlertStage.REMINDER) {
            val pendingTask = UserPrefs.getPendingTask(context, pkg)
            if (pendingTask.isNotEmpty()) {
                fullMessage += " Pending task: $pendingTask."
            }
        }
        
        // Voice alert if enabled
        if (UserPrefs.isVoiceAlertsEnabled(context)) {
            val tone = when (stage) {
                AlertStage.GENTLE -> VoiceAlertService.TONE_GENTLE
                AlertStage.REMINDER -> VoiceAlertService.TONE_REMINDER
                AlertStage.STRICT -> VoiceAlertService.TONE_STRICT
                else -> VoiceAlertService.TONE_GENTLE
            }
            VoiceAlertService.speak(context, fullMessage, tone)
        }

        // Show Overlay Alert
        if (stage != AlertStage.NONE) {
            // Check for Overlay Permission
            if (android.provider.Settings.canDrawOverlays(context)) {
                com.focusguardian.ui.AlertOverlayHelper.show(context, stage, pkg, fullMessage, appName)
            } else {
                // Fallback to Activity if permission missing, or just log/notify
                // For now, we attempt Activity as fallback ONLY if overlay fails/permission missing
                val intent = Intent(context, FocusAlertActivity::class.java).apply {
                    putExtra("stage", stage.name)
                    putExtra("message", fullMessage)
                    putExtra("pkg", pkg)
                    putExtra("appName", appName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                context.startActivity(intent)
            }
        }
    }

    // Deprecated methods
    fun dispatchGentle(context: Context, pkg: String, message: String) {
        dispatch(context, AlertStage.GENTLE, pkg)
    }

    fun dispatchReminder(context: Context, pkg: String, message: String) {
        dispatch(context, AlertStage.REMINDER, pkg)
    }

    fun dispatchStrict(context: Context, pkg: String, message: String) {
        dispatch(context, AlertStage.STRICT, pkg)
    }
}

