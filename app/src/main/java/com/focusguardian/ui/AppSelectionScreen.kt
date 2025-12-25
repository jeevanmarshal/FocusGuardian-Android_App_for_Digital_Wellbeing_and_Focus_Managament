package com.focusguardian.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.focusguardian.util.UserPrefs

class AppSelectionScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scrollView = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 30, 30, 30)
        }

        val title = TextView(this).apply {
            text = "Select Apps to Monitor"
            textSize = 20f
        }

        layout.addView(title)

        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        apps.sortedBy { it.loadLabel(pm).toString() }.forEach { app ->
            val appName = app.loadLabel(pm).toString()
            val pkg = app.packageName

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val checkBox = CheckBox(this).apply {
                isChecked = UserPrefs.isAppMonitored(this@AppSelectionScreen, pkg)
                setOnCheckedChangeListener { _, checked ->
                    UserPrefs.setAppMonitored(this@AppSelectionScreen, pkg, checked)
                }
            }

            val label = TextView(this).apply {
                text = appName
                setPadding(20, 0, 0, 0)
                setOnClickListener {
                    val intent = Intent(
                        this@AppSelectionScreen,
                        AppConfigScreen::class.java
                    )
                    intent.putExtra("pkg", pkg)
                    intent.putExtra("appName", appName)
                    startActivity(intent)
                }
            }

            row.addView(checkBox)
            row.addView(label)
            layout.addView(row)
        }

        scrollView.addView(layout)
        setContentView(scrollView)
    }
}
