package com.focusguardian.logic

import android.content.Context
import java.util.*

/**
 * Extracts behavioral features from usage events to infer cognitive states.
 */
object CognitiveFeatureExtractor {

    private val sessionStartTimes = mutableMapOf<String, Long>()
    private val sessionDurations = mutableListOf<Long>()
    private var lastSwitchTime: Long = 0
    private val switchIntervals = mutableListOf<Long>()

    fun recordAppSwitch(pkg: String) {
        val now = System.currentTimeMillis()
        
        // Track switch frequency
        if (lastSwitchTime > 0) {
            val interval = now - lastSwitchTime
            switchIntervals.add(interval)
            // Keep only last 20 intervals for rolling analysis
            if (switchIntervals.size > 20) switchIntervals.removeAt(0)
        }
        lastSwitchTime = now

        // Track session start
        sessionStartTimes[pkg] = now
    }

    fun getAverageSwitchInterval(): Long {
        if (switchIntervals.isEmpty()) return Long.MAX_VALUE
        return switchIntervals.average().toLong()
    }

    fun getShortSessionCount(thresholdMs: Long = 30000): Int {
        // Count how many intervals in the rolling window are "short"
        return switchIntervals.count { it < thresholdMs }
    }

    fun isLateNightUsage(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour in 0..5
    }

    fun reset() {
        sessionStartTimes.clear()
        sessionDurations.clear()
        switchIntervals.clear()
        lastSwitchTime = 0
    }
}
