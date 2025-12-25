package com.focusguardian.ui.alerts

import android.app.AlertDialog
import android.content.Context

object GentleAlertDialog {

    fun show(context: Context, pkg: String, message: String) {
        AlertDialog.Builder(context)
            .setTitle("Focus Reminder")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setCancelable(true)
            .show()
    }
}
