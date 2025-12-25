package com.focusguardian.ui

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.focusguardian.util.AnalyticsStore

class WeeklySummaryScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Weekly Focus Summary"
            textSize = 20f
        }

        layout.addView(title)

        val data = AnalyticsStore.getLast7Days(this)

        data.forEach { day ->
            val row = TextView(this).apply {
                text = """
                    ${day.date}
                    Focused: ${day.focusedMinutes} min
                    Distracted: ${day.distractedMinutes} min
                    Focus Score: ${day.focusScore()}%
                """.trimIndent()
                setPadding(0, 20, 0, 20)
            }
            layout.addView(row)
        }

        val avgScore = data.map { it.focusScore() }.average().toInt()

        val insight = TextView(this).apply {
            text = when {
                avgScore >= 80 -> "Excellent focus this week! 🎯"
                avgScore >= 60 -> "Good progress, stay consistent 👍"
                else -> "High distraction detected. Try stricter limits ⚠️"
            }
            textSize = 16f
        }

        layout.addView(insight)

        scroll.addView(layout)
        setContentView(scroll)
    }
}
