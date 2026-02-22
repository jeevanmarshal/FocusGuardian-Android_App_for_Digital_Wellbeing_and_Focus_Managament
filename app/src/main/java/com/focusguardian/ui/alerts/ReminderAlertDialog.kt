package com.focusguardian.ui.alerts

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.provider.Settings
import com.focusguardian.util.UserPrefs

object ReminderAlertDialog {

    fun show(context: Context, pkg: String, message: String) {
        val appName = UserPrefs.getAppName(context, pkg)
        val customMessage = UserPrefs.getReminderMessage(context, pkg)
        val pendingTask = UserPrefs.getPendingTask(context, pkg)
        
        val fullMessage = if (pendingTask.isNotEmpty()) {
            "$appName\n\n$customMessage\n\nPending Task: $pendingTask"
        } else {
            "$appName\n\n$customMessage"
        }
        
        // Play loud notification sound
        playAlertSound(context)
        
        AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("⚠️ AI Coaching Hint")
            .setMessage(fullMessage)
            .setPositiveButton("TAKE BREAK") { _, _ ->
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            .setNegativeButton("NOT NOW", null)
            .setCancelable(false)
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
