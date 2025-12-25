package com.focusguardian.util

import android.content.Context
import android.content.SharedPreferences

object UserPrefs {

    private const val PREF_NAME = "focus_guardian_prefs"

    // ---------- GLOBAL KEYS ----------
    private const val KEY_APP_MONITORING = "app_monitoring"
    private const val KEY_VOICE_ALERTS = "voice_alerts"
    private const val KEY_STRICT_GLOBAL = "strict_global"
    private const val KEY_EMERGENCY_PAUSE_UNTIL = "emergency_pause_until"

    // ---------- DEFAULT VALUES ----------
    const val DEFAULT_GENTLE_MIN = 20
    const val DEFAULT_REMINDER_MIN = 10
    const val DEFAULT_STRICT_MIN = 5

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /* ==========================================================
       🌍 GLOBAL SETTINGS
       ========================================================== */

    fun isAppMonitoringEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_APP_MONITORING, true)

    fun setAppMonitoringEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_APP_MONITORING, enabled).apply()
    }

    fun isVoiceAlertsEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_VOICE_ALERTS, true)

    fun setVoiceAlertsEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_VOICE_ALERTS, enabled).apply()
    }

    fun isGlobalStrictEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_STRICT_GLOBAL, true)

    fun setGlobalStrictEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_STRICT_GLOBAL, enabled).apply()
    }

    /* ==========================================================
       ⏸️ EMERGENCY PAUSE
       ========================================================== */

    fun setEmergencyPause(context: Context, minutes: Int) {
        val pauseUntil = System.currentTimeMillis() + minutes * 60_000L
        prefs(context).edit().putLong(KEY_EMERGENCY_PAUSE_UNTIL, pauseUntil).apply()
    }

    fun isEmergencyPauseActive(context: Context): Boolean {
        val until = prefs(context).getLong(KEY_EMERGENCY_PAUSE_UNTIL, 0L)
        return System.currentTimeMillis() < until
    }

    /* ==========================================================
       📱 PER-APP SETTINGS
       ========================================================== */

    private fun key(pkg: String, suffix: String) = "$pkg-$suffix"

    fun setAppMonitored(context: Context, pkg: String, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(key(pkg, "monitored"), enabled)
            .apply()
    }

    fun isAppMonitored(context: Context, pkg: String): Boolean =
        prefs(context).getBoolean(key(pkg, "monitored"), false)

    /* ---------- TIME CONFIGURATION (X, Y, Z) ---------- */

    fun setGentleTime(context: Context, pkg: String, minutes: Int) {
        prefs(context).edit()
            .putInt(key(pkg, "gentle_time"), minutes)
            .apply()
    }

    fun getGentleTime(context: Context, pkg: String): Int =
        prefs(context).getInt(key(pkg, "gentle_time"), DEFAULT_GENTLE_MIN)

    fun setReminderTime(context: Context, pkg: String, minutes: Int) {
        prefs(context).edit()
            .putInt(key(pkg, "reminder_time"), minutes)
            .apply()
    }

    fun getReminderTime(context: Context, pkg: String): Int =
        prefs(context).getInt(key(pkg, "reminder_time"), DEFAULT_REMINDER_MIN)

    fun setStrictTime(context: Context, pkg: String, minutes: Int) {
        prefs(context).edit()
            .putInt(key(pkg, "strict_time"), minutes)
            .apply()
    }

    fun getStrictTime(context: Context, pkg: String): Int =
        prefs(context).getInt(key(pkg, "strict_time"), DEFAULT_STRICT_MIN)

    /* ---------- ALERT MESSAGES ---------- */

    fun setGentleMessage(context: Context, pkg: String, msg: String) {
        prefs(context).edit()
            .putString(key(pkg, "gentle_msg"), msg)
            .apply()
    }

    fun getGentleMessage(context: Context, pkg: String): String =
        prefs(context).getString(
            key(pkg, "gentle_msg"),
            "Time to focus!"
        ) ?: "Time to focus!"

    fun setReminderMessage(context: Context, pkg: String, msg: String) {
        prefs(context).edit()
            .putString(key(pkg, "reminder_msg"), msg)
            .apply()
    }

    fun getReminderMessage(context: Context, pkg: String): String =
        prefs(context).getString(
            key(pkg, "reminder_msg"),
            "Get back to your important task"
        ) ?: "Get back to your important task"

    fun setStrictMessage(context: Context, pkg: String, msg: String) {
        prefs(context).edit()
            .putString(key(pkg, "strict_msg"), msg)
            .apply()
    }

    fun getStrictMessage(context: Context, pkg: String): String =
        prefs(context).getString(
            key(pkg, "strict_msg"),
            "You must stop using this app now"
        ) ?: "You must stop using this app now"

    /* ---------- ENFORCEMENT OPTIONS ---------- */

    fun setStrictEnabled(context: Context, pkg: String, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(key(pkg, "strict_enabled"), enabled)
            .apply()
    }

    fun isStrictEnabled(context: Context, pkg: String): Boolean =
        prefs(context).getBoolean(key(pkg, "strict_enabled"), true)

    fun setEmergencyUnlockAllowed(context: Context, pkg: String, allowed: Boolean) {
        prefs(context).edit()
            .putBoolean(key(pkg, "emergency_unlock"), allowed)
            .apply()
    }

    fun isEmergencyUnlockAllowed(context: Context, pkg: String): Boolean =
        prefs(context).getBoolean(key(pkg, "emergency_unlock"), true)
}
