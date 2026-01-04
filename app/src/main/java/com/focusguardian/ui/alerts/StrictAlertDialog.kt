package com.focusguardian.ui.alerts

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import com.focusguardian.util.UserPrefs

object StrictAlertDialog {

    fun show(context: Context, pkg: String, message: String) {
        val appName = UserPrefs.getAppName(context, pkg)
        val customMessage = UserPrefs.getStrictMessage(context, pkg)
        
        // Play loud notification sound
        playAlertSound(context)

        AlertDialog.Builder(context, android.R.style.Theme_DeviceDefault_Dialog_Alert)
            .setTitle("🚫 Focus Enforcement")
            .setMessage("$appName\n\n$customMessage\n\nRedirecting to preserve your focus.")
            .setCancelable(false)
            .show()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }, 2000)
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
