package com.focusguardian.util

import android.content.Context
import com.focusguardian.model.DailyUsage

object AnalyticsStore {

    private const val PREF = "weekly_analytics"

    fun addFocusedTime(context: Context, seconds: Int) {
        updateUsage(context, seconds, 0)
    }

    fun addDistractedTime(context: Context, seconds: Int) {
        updateUsage(context, 0, seconds)
    }

    fun addCategoryUsage(context: Context, categoryName: String, seconds: Int) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val date = java.time.LocalDate.now().toString()
        val key = "${date}_cat_${categoryName.lowercase()}"
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + seconds).apply()
    }

    private fun updateUsage(context: Context, focusedSec: Int, distractedSec: Int) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val date = java.time.LocalDate.now().toString()

        val currentFocused = prefs.getInt("${date}_focused_sec", 0)
        val currentDistracted = prefs.getInt("${date}_distracted_sec", 0)

        prefs.edit()
            .putInt("${date}_focused_sec", currentFocused + focusedSec)
            .putInt("${date}_distracted_sec", currentDistracted + distractedSec)
            .apply()
    }

    fun getLast7Days(context: Context): List<DailyUsage> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val list = mutableListOf<DailyUsage>()

        for (i in 0..6) {
            val date = java.time.LocalDate.now().minusDays(i.toLong()).toString()
            val focusedSec = prefs.getInt("${date}_focused_sec", 0)
            val distractedSec = prefs.getInt("${date}_distracted_sec", 0)

            list.add(DailyUsage(date, focusedSec / 60, distractedSec / 60))
        }
        return list.reversed()
    }
}
