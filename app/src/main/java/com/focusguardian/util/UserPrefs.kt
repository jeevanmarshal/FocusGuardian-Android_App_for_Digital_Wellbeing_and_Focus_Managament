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
    private const val KEY_MANUAL_PAUSE = "manual_pause"
    private const val KEY_GLOBAL_INTENT = "global_intent"
    private const val KEY_SWITCH_COUNT_TODAY = "switch_count_today"

    // ---------- DEFAULT VALUES ----------
    const val DEFAULT_GENTLE_MIN = 5
    const val DEFAULT_REMINDER_MIN = 5
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

    private const val KEY_ALERT_VOICE_TYPE = "alert_voice_type"

    fun setAlertVoiceType(context: Context, type: String) {
        prefs(context).edit().putString(KEY_ALERT_VOICE_TYPE, type).apply()
    }

    fun getAlertVoiceType(context: Context): String =
        prefs(context).getString(KEY_ALERT_VOICE_TYPE, "FEMALE") ?: "FEMALE"

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

    fun setManualPause(context: Context, paused: Boolean) {
        prefs(context).edit().putBoolean(KEY_MANUAL_PAUSE, paused).apply()
    }

    fun isManualPauseActive(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MANUAL_PAUSE, false)

    fun isEmergencyPauseActive(context: Context): Boolean {
        if (isManualPauseActive(context)) return true
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

    fun setAppName(context: Context, pkg: String, name: String) {
        prefs(context).edit().putString(key(pkg, "app_name"), name).apply()
    }

    fun getAppName(context: Context, pkg: String): String =
        prefs(context).getString(key(pkg, "app_name"), pkg) ?: pkg

    /* ---------- TIME CONFIGURATION (X, Y, Z) ---------- */

    /* ---------- TIME CONFIGURATION (Seconds Support) ---------- */

    fun setGentleSeconds(context: Context, pkg: String, seconds: Long) {
        prefs(context).edit().putLong(key(pkg, "gentle_seconds"), seconds).apply()
    }

    fun getGentleSeconds(context: Context, pkg: String): Long {
        val seconds = prefs(context).getLong(key(pkg, "gentle_seconds"), -1L)
        if (seconds != -1L) return seconds
        // Fallback to legacy minutes
        val mins = prefs(context).getInt(key(pkg, "gentle_time"), DEFAULT_GENTLE_MIN)
        return mins * 60L
    }

    fun setReminderSeconds(context: Context, pkg: String, seconds: Long) {
        prefs(context).edit().putLong(key(pkg, "reminder_seconds"), seconds).apply()
    }

    fun getReminderSeconds(context: Context, pkg: String): Long {
        val seconds = prefs(context).getLong(key(pkg, "reminder_seconds"), -1L)
        if (seconds != -1L) return seconds
        val mins = prefs(context).getInt(key(pkg, "reminder_time"), DEFAULT_REMINDER_MIN)
        return mins * 60L
    }

    fun setStrictSeconds(context: Context, pkg: String, seconds: Long) {
         prefs(context).edit().putLong(key(pkg, "strict_seconds"), seconds).apply()
    }

    fun getStrictSeconds(context: Context, pkg: String): Long {
        val seconds = prefs(context).getLong(key(pkg, "strict_seconds"), -1L)
        if (seconds != -1L) return seconds
        val mins = prefs(context).getInt(key(pkg, "strict_time"), DEFAULT_STRICT_MIN)
        return mins * 60L
    }

    // LEGACY WRAPPERS (Minutes)
    fun setGentleTime(context: Context, pkg: String, minutes: Int) {
        setGentleSeconds(context, pkg, minutes * 60L)
    }

    fun getGentleTime(context: Context, pkg: String): Int =
        (getGentleSeconds(context, pkg) / 60).toInt()

    fun setReminderTime(context: Context, pkg: String, minutes: Int) {
        setReminderSeconds(context, pkg, minutes * 60L)
    }

    fun getReminderTime(context: Context, pkg: String): Int =
        (getReminderSeconds(context, pkg) / 60).toInt()

    fun setStrictTime(context: Context, pkg: String, minutes: Int) {
        setStrictSeconds(context, pkg, minutes * 60L)
    }

    fun getStrictTime(context: Context, pkg: String): Int =
         (getStrictSeconds(context, pkg) / 60).toInt()

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
            "Get back to your task."
        ) ?: "Get back to your task."

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

    fun setGentleEnabled(context: Context, pkg: String, enabled: Boolean) {
        prefs(context).edit().putBoolean(key(pkg, "gentle_enabled"), enabled).apply()
    }

    fun isGentleEnabled(context: Context, pkg: String): Boolean =
        prefs(context).getBoolean(key(pkg, "gentle_enabled"), true)

    fun setReminderEnabled(context: Context, pkg: String, enabled: Boolean) {
        prefs(context).edit().putBoolean(key(pkg, "reminder_enabled"), enabled).apply()
    }

    fun isReminderEnabled(context: Context, pkg: String): Boolean =
        prefs(context).getBoolean(key(pkg, "reminder_enabled"), true)

    /* ---------- INTENT & AI ADAPTIVE ---------- */

    fun setAppIntent(context: Context, pkg: String, intent: String) {
        prefs(context).edit().putString(key(pkg, "intent"), intent).apply()
    }

    fun getAppIntent(context: Context, pkg: String): String =
        prefs(context).getString(key(pkg, "intent"), "SOCIAL") ?: "SOCIAL"

    fun setAIAdaptiveEnabled(context: Context, pkg: String, enabled: Boolean) {
        prefs(context).edit().putBoolean(key(pkg, "ai_adaptive"), enabled).apply()
    }

    fun isAIAdaptiveEnabled(context: Context, pkg: String): Boolean =
        prefs(context).getBoolean(key(pkg, "ai_adaptive"), false)

    fun setCognitiveSensitivity(context: Context, pkg: String, level: Int) {
        prefs(context).edit().putInt(key(pkg, "sensitivity"), level).apply()
    }

    fun getCognitiveSensitivity(context: Context, pkg: String): Int =
        prefs(context).getInt(key(pkg, "sensitivity"), 1) // 1: Low, 2: Medium, 3: High

    fun setGlobalIntent(context: Context, intent: String) {
        prefs(context).edit().putString(KEY_GLOBAL_INTENT, intent).apply()
    }

    fun getGlobalIntent(context: Context): String =
        prefs(context).getString(KEY_GLOBAL_INTENT, "GENERAL") ?: "GENERAL"

    fun incrementSwitchCount(context: Context) {
        val current = prefs(context).getInt(KEY_SWITCH_COUNT_TODAY, 0)
        prefs(context).edit().putInt(KEY_SWITCH_COUNT_TODAY, current + 1).apply()
    }

    fun getSwitchCount(context: Context): Int =
        prefs(context).getInt(KEY_SWITCH_COUNT_TODAY, 0)

    fun resetSwitchCount(context: Context) {
        prefs(context).edit().putInt(KEY_SWITCH_COUNT_TODAY, 0).apply()
    }

    /* ---------- PENDING TASKS ---------- */

    fun setPendingTask(context: Context, pkg: String, task: String) {
        prefs(context).edit().putString(key(pkg, "pending_task"), task).apply()
    }

    fun getPendingTask(context: Context, pkg: String): String =
        prefs(context).getString(key(pkg, "pending_task"), "") ?: ""

    /* ---------- USAGE FREQUENCY ---------- */

    fun incrementAppUsageCount(context: Context, pkg: String) {
        val current = prefs(context).getInt(key(pkg, "usage_count"), 0)
        prefs(context).edit().putInt(key(pkg, "usage_count"), current + 1).apply()
    }

    fun getAppUsageCount(context: Context, pkg: String): Int =
        prefs(context).getInt(key(pkg, "usage_count"), 0)

    fun setLastUsedTimestamp(context: Context, pkg: String) {
        prefs(context).edit().putLong(key(pkg, "last_used"), System.currentTimeMillis()).apply()
    }

    fun getLastUsedTimestamp(context: Context, pkg: String): Long =
        prefs(context).getLong(key(pkg, "last_used"), 0L)

    /* ---------- APP PAUSE / BLOCK LOGIC ---------- */

    fun setAppPausedUntil(context: Context, pkg: String, timestamp: Long) {
        prefs(context).edit().putLong(key(pkg, "paused_until"), timestamp).apply()
    }

    fun getAppPausedUntil(context: Context, pkg: String): Long =
        prefs(context).getLong(key(pkg, "paused_until"), 0L)

    fun isAppPaused(context: Context, pkg: String): Boolean {
        return System.currentTimeMillis() < getAppPausedUntil(context, pkg)
    }

    fun setPauseDurationMinutes(context: Context, pkg: String, minutes: Int) {
        prefs(context).edit().putInt(key(pkg, "pause_duration"), minutes).apply()
    }

    fun getPauseDurationMinutes(context: Context, pkg: String): Int =
        prefs(context).getInt(key(pkg, "pause_duration"), 5) // Default 5 minutes

    /* ---------- THEME ---------- */

    // 0: System, 1: Light, 2: Dark
    fun setThemeMode(context: Context, mode: Int) {
        prefs(context).edit().putInt("theme_mode", mode).apply()
    }

    fun getThemeMode(context: Context): Int =
        prefs(context).getInt("theme_mode", 0)

    /* ---------- FOCUS MODE / BEDTIME ---------- */
    private const val KEY_FOCUS_MODE_ACTIVE = "focus_mode_active"
    private const val KEY_FOCUS_MODE_END = "focus_mode_end"
    private const val KEY_FOCUS_APPS = "focus_mode_apps" // Set<String>

    fun setFocusModeActive(context: Context, active: Boolean, durationMinutes: Int = 0) {
        val editor = prefs(context).edit()
        editor.putBoolean(KEY_FOCUS_MODE_ACTIVE, active)
        if (active && durationMinutes > 0) {
            editor.putLong(KEY_FOCUS_MODE_END, System.currentTimeMillis() + durationMinutes * 60_000L)
        } else {
            editor.putLong(KEY_FOCUS_MODE_END, 0)
        }
        editor.apply()
    }

    fun isFocusModeActive(context: Context): Boolean {
        if (prefs(context).getBoolean(KEY_FOCUS_MODE_ACTIVE, false)) {
            val endTime = prefs(context).getLong(KEY_FOCUS_MODE_END, 0)
            if (endTime != 0L && System.currentTimeMillis() > endTime) {
                setFocusModeActive(context, false)
                return false
            }
            return true
        }
        return false
    }

    fun setFocusModeApps(context: Context, apps: Set<String>) {
        prefs(context).edit().putStringSet(KEY_FOCUS_APPS, apps).apply()
    }

    fun getFocusModeApps(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_FOCUS_APPS, emptySet()) ?: emptySet()
    
    fun isAppInFocusMode(context: Context, pkg: String): Boolean {
        return getFocusModeApps(context).contains(pkg)
    }

    /* ---------- BEDTIME ---------- */
    private const val KEY_BEDTIME_ENABLED = "bedtime_enabled"
    private const val KEY_BEDTIME_START_H = "bedtime_start_h"
    private const val KEY_BEDTIME_START_M = "bedtime_start_m"
    private const val KEY_BEDTIME_END_H = "bedtime_end_h"
    private const val KEY_BEDTIME_END_M = "bedtime_end_m"

    fun setBedtimeEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BEDTIME_ENABLED, enabled).apply()
    }

    fun isBedtimeEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BEDTIME_ENABLED, false)

    fun setBedtimeStart(context: Context, h: Int, m: Int) {
        prefs(context).edit()
            .putInt(KEY_BEDTIME_START_H, h)
            .putInt(KEY_BEDTIME_START_M, m)
            .apply()
    }

    fun getBedtimeStartHour(context: Context): Int = prefs(context).getInt(KEY_BEDTIME_START_H, 22)
    fun getBedtimeStartMinute(context: Context): Int = prefs(context).getInt(KEY_BEDTIME_START_M, 0)

    fun setBedtimeEnd(context: Context, h: Int, m: Int) {
        prefs(context).edit()
            .putInt(KEY_BEDTIME_END_H, h)
            .putInt(KEY_BEDTIME_END_M, m)
            .apply()
    }

    fun getBedtimeEndHour(context: Context): Int = prefs(context).getInt(KEY_BEDTIME_END_H, 7)
    fun getBedtimeEndMinute(context: Context): Int = prefs(context).getInt(KEY_BEDTIME_END_M, 0)
}
