package com.focusguardian.util

import android.content.Context

object FocusScoreUtil {

    fun getTodayScore(context: Context): Int {
        val today = AnalyticsStore.getLast7Days(context).lastOrNull()
        return today?.focusScore() ?: 0
    }

    fun getFocusedTime(context: Context): Int {
        val today = AnalyticsStore.getLast7Days(context).lastOrNull()
        return today?.focusedMinutes ?: 0
    }

    fun getDistractedTime(context: Context): Int {
        val today = AnalyticsStore.getLast7Days(context).lastOrNull()
        return today?.distractedMinutes ?: 0
    }
}
