package com.focusguardian.ui.alerts

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings

object ReminderAlertDialog {

    fun show(context: Context, pkg: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Task Reminder")
            .setMessage(message)
            .setPositiveButton("Go to Task") { _, _ ->
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            .setNegativeButton("Ignore", null)
            .setCancelable(false)
            .show()
    }
}
