package com.focusguardian.util

import android.content.Context

object FocusScoreUtil {

    fun getTodayScore(context: Context): Int {
        return 75 // placeholder, replace with real logic later
    }

    fun getFocusedTime(context: Context): Int {
        return 120
    }

    fun getDistractedTime(context: Context): Int {
        return 40
    }
}
