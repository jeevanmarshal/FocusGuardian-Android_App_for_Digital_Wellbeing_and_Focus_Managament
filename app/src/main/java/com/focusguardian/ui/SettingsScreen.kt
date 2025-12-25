/*
package com.focusguardian.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.focusguardian.util.UserPrefs

class SettingsScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Settings"
            textSize = 20f
        }

        val monitoringSwitch = Switch(this).apply {
            text = "App Monitoring"
            isChecked = UserPrefs.isAppMonitoringEnabled(this@SettingsScreen)
            setOnCheckedChangeListener { _, checked ->
                UserPrefs.setAppMonitoringEnabled(this@SettingsScreen, checked)
            }
        }

        val voiceSwitch = Switch(this).apply {
            text = "Voice Alerts"
            isChecked = UserPrefs.isVoiceAlertsEnabled(this@SettingsScreen)
            setOnCheckedChangeListener { _, checked ->
                UserPrefs.setVoiceAlertsEnabled(this@SettingsScreen, checked)
            }
        }

        val strictSwitch = Switch(this).apply {
            text = "Strict Mode"
            isChecked = UserPrefs.isGlobalStrictEnabled(this@SettingsScreen)
            setOnCheckedChangeListener { _, checked ->
                UserPrefs.setGlobalStrictEnabled(this@SettingsScreen, checked)
            }
        }

        val pause15 = Button(this).apply {
            text = "Pause for 15 Minutes"
            setOnClickListener {
                UserPrefs.setEmergencyPause(this@SettingsScreen, 15)
                Toast.makeText(context, "Paused for 15 minutes", Toast.LENGTH_SHORT).show()
            }
        }

        val pause30 = Button(this).apply {
            text = "Pause for 30 Minutes"
            setOnClickListener {
                UserPrefs.setEmergencyPause(this@SettingsScreen, 30)
                Toast.makeText(context, "Paused for 30 minutes", Toast.LENGTH_SHORT).show()
            }
        }

        layout.addView(title)
        layout.addView(monitoringSwitch)
        layout.addView(voiceSwitch)
        layout.addView(strictSwitch)
        layout.addView(pause15)
        layout.addView(pause30)

        setContentView(layout)
    }
}
val permissionBtn = Button(this).apply {
    text = "Permission Health"
    setOnClickListener {
        startActivity(
            android.content.Intent(
                this@SettingsScreen,
                PermissionHealthScreen::class.java
            )
        )
    }
}
layout.addView(permissionBtn)

*/

package com.focusguardian.ui

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val text = TextView(this)
        text.text = "Settings Screen (Coming Soon)"
        text.textSize = 18f

        setContentView(text)
    }
}
