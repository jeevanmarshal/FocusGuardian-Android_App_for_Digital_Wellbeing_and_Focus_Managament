package com.focusguardian.util

import android.content.Context
import com.focusguardian.model.DailyUsage

object AnalyticsStore {

    private const val PREF = "weekly_analytics"

    fun saveTodayUsage(
        context: Context,
        focused: Int,
        distracted: Int
    ) {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val date = java.time.LocalDate.now().toString()

        prefs.edit()
            .putInt("${date}_focused", focused)
            .putInt("${date}_distracted", distracted)
            .apply()
    }

    fun getLast7Days(context: Context): List<DailyUsage> {
        val prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val list = mutableListOf<DailyUsage>()

        for (i in 0..6) {
            val date = java.time.LocalDate.now().minusDays(i.toLong()).toString()
            val focused = prefs.getInt("${date}_focused", 0)
            val distracted = prefs.getInt("${date}_distracted", 0)

            list.add(DailyUsage(date, focused, distracted))
        }
        return list.reversed()
    }
}
