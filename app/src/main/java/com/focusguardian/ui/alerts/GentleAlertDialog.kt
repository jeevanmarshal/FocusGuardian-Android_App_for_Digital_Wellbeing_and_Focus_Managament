package com.focusguardian.ui.alerts

import android.app.AlertDialog
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.focusguardian.util.UserPrefs

object GentleAlertDialog {

    fun show(context: Context, pkg: String, message: String) {
        val appName = UserPrefs.getAppName(context, pkg)
        val customMessage = UserPrefs.getGentleMessage(context, pkg)
        
        // Play loud notification sound
        playAlertSound(context)
        
        AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("🧠 AI Focus Coach")
            .setMessage("$appName\n\n$customMessage")
            .setPositiveButton("I UNDERSTAND", null)
            .setCancelable(true)
            .show()
    }
    
    private fun playAlertSound(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val ringtone = RingtoneManager.getRingtone(context, notification)
            
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.audioAttributes = audioAttributes
            
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
