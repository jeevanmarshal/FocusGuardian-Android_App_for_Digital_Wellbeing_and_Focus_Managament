package com.focusguardian.ui

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
// import com.focusguardian.util.FocusScoreUtil

class DashboardScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Focus Guardian"
            textSize = 22f
        }

        val focusScore = TextView(this).apply {
    textSize = 18f
    text = "Focus Score: --"
}

val focusTime = TextView(this).apply {
    text = "Focused Time: -- min"
}

val distractedTime = TextView(this).apply {
    text = "Distracted Time: -- min"
}

        val settingsBtn = Button(this).apply {
            text = "Settings"
            setOnClickListener {
                startActivity(Intent(this@DashboardScreen, SettingsScreen::class.java))
            }
        }

        layout.addView(title)
        layout.addView(focusScore)
        layout.addView(focusTime)
        layout.addView(distractedTime)
        layout.addView(settingsBtn)

        setContentView(layout)
    }
}
      