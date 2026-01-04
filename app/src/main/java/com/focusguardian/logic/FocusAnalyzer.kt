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
        val distractedMinutes = usageMap.values.sum()
        
        // Convert to seconds since the new API expects seconds
        AnalyticsStore.addDistractedTime(context, distractedMinutes * 60)
    }

    fun reset() {
        usageMap.clear()
    }
}
