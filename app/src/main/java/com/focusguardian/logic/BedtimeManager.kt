package com.focusguardian.logic

import android.content.Context
import com.focusguardian.util.UserPrefs
import java.util.Calendar

object BedtimeManager {

    fun isBedtimeActive(context: Context): Boolean {
        if (!UserPrefs.isBedtimeEnabled(context)) return false

        val startH = UserPrefs.getBedtimeStartHour(context)
        val startM = UserPrefs.getBedtimeStartMinute(context)
        val endH = UserPrefs.getBedtimeEndHour(context)
        val endM = UserPrefs.getBedtimeEndMinute(context)

        val now = Calendar.getInstance()
        val currentH = now.get(Calendar.HOUR_OF_DAY)
        val currentM = now.get(Calendar.MINUTE)

        val currentMins = currentH * 60 + currentM
        val startMins = startH * 60 + startM
        val endMins = endH * 60 + endM

        return if (startMins <= endMins) {
            // Same day interval (e.g. 10:00 - 20:00)
            currentMins in startMins..endMins
        } else {
            // Overnight interval (e.g. 22:00 - 07:00)
            currentMins >= startMins || currentMins <= endMins
        }
    }

    fun getRestrictedApps(context: Context): Set<String> {
        // For Bedtime, we might want to restrict ALL apps except Whitelist, 
        // or just reuse Focus Mode apps. 
        // Prompt says: "App restrictions".
        // Let's use Focus Mode apps as the default set for Bedtime too for simplicity.
        return UserPrefs.getFocusModeApps(context)
    }
}
