package com.focusguardian.logic

import android.content.Context
import android.content.Intent
import com.focusguardian.service.VoiceAlertService
import com.focusguardian.ui.alerts.GentleAlertDialog
import com.focusguardian.ui.alerts.ReminderAlertDialog
import com.focusguardian.ui.alerts.StrictAlertDialog
import com.focusguardian.util.UserPrefs

object AlertDispatcher {

    fun dispatchGentle(context: Context, pkg: String, message: String) {
        if (UserPrefs.isVoiceAlertsEnabled(context)) {
            VoiceAlertService.speak(context, message, VoiceAlertService.TONE_GENTLE)
        }

        GentleAlertDialog.show(context, pkg, message)
    }

    fun dispatchReminder(context: Context, pkg: String, message: String) {
        if (UserPrefs.isVoiceAlertsEnabled(context)) {
            VoiceAlertService.speak(context, message, VoiceAlertService.TONE_REMINDER)
        }

        ReminderAlertDialog.show(context, pkg, message)
    }

    fun dispatchStrict(context: Context, pkg: String, message: String) {
        if (UserPrefs.isVoiceAlertsEnabled(context)) {
            VoiceAlertService.speak(context, message, VoiceAlertService.TONE_STRICT)
        }

        StrictAlertDialog.show(context, pkg, message)
    }
}
