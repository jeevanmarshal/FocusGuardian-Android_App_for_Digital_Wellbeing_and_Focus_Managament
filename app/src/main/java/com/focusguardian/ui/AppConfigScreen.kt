package com.focusguardian.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.focusguardian.util.UserPrefs

class AppConfigScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pkg = intent.getStringExtra("pkg") ?: return
        val appName = intent.getStringExtra("appName") ?: pkg

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val title = TextView(this).apply {
            text = "Configure $appName"
            textSize = 20f
        }

        /* ----------- TIME SETTINGS ----------- */

        val gentleInput = EditText(this).apply {
            hint = "Gentle alert after X minutes"
            setText(UserPrefs.getGentleTime(this@AppConfigScreen, pkg).toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val reminderInput = EditText(this).apply {
            hint = "Reminder after Y minutes"
            setText(UserPrefs.getReminderTime(this@AppConfigScreen, pkg).toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val strictInput = EditText(this).apply {
            hint = "Strict alert after Z minutes"
            setText(UserPrefs.getStrictTime(this@AppConfigScreen, pkg).toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        /* ----------- MESSAGE SETTINGS ----------- */

        val gentleMsg = EditText(this).apply {
            hint = "Gentle alert message"
            setText(UserPrefs.getGentleMessage(this@AppConfigScreen, pkg))
        }

        val reminderMsg = EditText(this).apply {
            hint = "Reminder task message"
            setText(UserPrefs.getReminderMessage(this@AppConfigScreen, pkg))
        }

        /* ----------- TOGGLES ----------- */

        val strictToggle = Switch(this).apply {
            text = "Enable Strict Enforcement"
            isChecked = UserPrefs.isStrictEnabled(this@AppConfigScreen, pkg)
        }

        val emergencyToggle = Switch(this).apply {
            text = "Allow Emergency Unlock"
            isChecked = UserPrefs.isEmergencyUnlockAllowed(this@AppConfigScreen, pkg)
        }

        /* ----------- SAVE BUTTON ----------- */

        val saveBtn = Button(this).apply {
            text = "Save Settings"
            setOnClickListener {
                UserPrefs.setGentleTime(
                    this@AppConfigScreen, pkg,
                    gentleInput.text.toString().toIntOrNull() ?: 20
                )
                UserPrefs.setReminderTime(
                    this@AppConfigScreen, pkg,
                    reminderInput.text.toString().toIntOrNull() ?: 10
                )
                UserPrefs.setStrictTime(
                    this@AppConfigScreen, pkg,
                    strictInput.text.toString().toIntOrNull() ?: 5
                )

                UserPrefs.setGentleMessage(this@AppConfigScreen, pkg, gentleMsg.text.toString())
                UserPrefs.setReminderMessage(this@AppConfigScreen, pkg, reminderMsg.text.toString())

                UserPrefs.setStrictEnabled(this@AppConfigScreen, pkg, strictToggle.isChecked)
                UserPrefs.setEmergencyUnlockAllowed(this@AppConfigScreen, pkg, emergencyToggle.isChecked)

                Toast.makeText(
                    this@AppConfigScreen,
                    "Settings saved for $appName",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            }
        }

        /* ----------- ADD VIEWS ----------- */

        layout.addView(title)
        layout.addView(gentleInput)
        layout.addView(reminderInput)
        layout.addView(strictInput)
        layout.addView(gentleMsg)
        layout.addView(reminderMsg)
        layout.addView(strictToggle)
        layout.addView(emergencyToggle)
        layout.addView(saveBtn)

        scroll.addView(layout)
        setContentView(scroll)
    }
}
