package com.focusguardian.ui.alerts

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

object StrictAlertDialog {

    fun show(context: Context, pkg: String, message: String) {

        AlertDialog.Builder(context)
            .setTitle("Strict Focus Alert")
            .setMessage(message + "\n\nYou will be redirected.")
            .setCancelable(false)
            .show()

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }, 2000)
    }
}
