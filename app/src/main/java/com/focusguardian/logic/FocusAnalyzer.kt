package com.focusguardian.logic

import android.content.Context
import com.focusguardian.util.AnalyticsStore
import com.focusguardian.util.UserPrefs

object FocusAnalyzer {

    private val usageMap = mutableMapOf<String, Int>()

    fun recordUsage(
        context: Context,
        pkg: String
    ): Int {
        val minutes = (usageMap[pkg] ?: 0) + 1
        usageMap[pkg] = minutes
        return minutes
    }

    fun saveDailyAnalytics(context: Context) {
        val distracted = usageMap.values.sum()
        val focused = 24 * 60 - distracted

        AnalyticsStore.saveTodayUsage(
            context = context,
            focused = focused.coerceAtLeast(0),
            distracted = distracted
        )
    }

    fun reset() {
        usageMap.clear()
    }
}
