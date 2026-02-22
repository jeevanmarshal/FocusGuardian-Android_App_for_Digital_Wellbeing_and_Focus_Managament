package com.focusguardian.domain.strict

import android.content.Context
import android.content.SharedPreferences

class EmergencyUnlockManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("emergency_unlock_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_EMERGENCY_UNLOCKED = "is_emergency_unlocked"
        private const val UNLOCK_DURATION_MS = 5 * 60 * 1000L // 5 minutes override
        private const val KEY_UNLOCK_TIMESTAMP = "unlock_timestamp"
    }

    fun isEmergencyUnlocked(): Boolean {
        val isUnlocked = prefs.getBoolean(KEY_EMERGENCY_UNLOCKED, false)
        if (isUnlocked) {
            val timestamp = prefs.getLong(KEY_UNLOCK_TIMESTAMP, 0L)
            if (System.currentTimeMillis() - timestamp > UNLOCK_DURATION_MS) {
                // Expired
                lock()
                return false
            }
            return true
        }
        return false
    }

    fun unlock() {
        prefs.edit()
            .putBoolean(KEY_EMERGENCY_UNLOCKED, true)
            .putLong(KEY_UNLOCK_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun lock() {
        prefs.edit()
            .putBoolean(KEY_EMERGENCY_UNLOCKED, false)
            .remove(KEY_UNLOCK_TIMESTAMP)
            .apply()
    }
}
