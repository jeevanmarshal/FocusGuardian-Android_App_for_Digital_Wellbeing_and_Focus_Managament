package com.focusguardian.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_guardian_prefs")

class LocalStorageManager(private val context: Context) {

    companion object {
        val KEY_STRICT_MODE_ACTIVE = booleanPreferencesKey("strict_mode_active")
        val KEY_STRICT_MODE_START = longPreferencesKey("strict_mode_start")
        val KEY_STRICT_MODE_REASON = stringPreferencesKey("strict_mode_reason")
        
        val KEY_ALERT_STAGE_GENTLE = booleanPreferencesKey("alert_stage_gentle")
        val KEY_ALERT_STAGE_REMINDER = booleanPreferencesKey("alert_stage_reminder")
        val KEY_ALERT_STAGE_STRICT = booleanPreferencesKey("alert_stage_strict")
        
        // Block state could be stored, but BlockStateManager often re-evaluates dynamically.
        // However, persistent block reason is good for restore.
        val KEY_LAST_BLOCK_REASON = stringPreferencesKey("last_block_reason")
    }

    // Strict Mode State
    val strictModeActiveFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_STRICT_MODE_ACTIVE] ?: false }

    suspend fun saveStrictModeState(isActive: Boolean, startTime: Long = 0, reason: String = "") {
        context.dataStore.edit { preferences ->
            preferences[KEY_STRICT_MODE_ACTIVE] = isActive
            preferences[KEY_STRICT_MODE_START] = startTime
            preferences[KEY_STRICT_MODE_REASON] = reason
        }
    }
    
    // Alert State
    suspend fun saveAlertState(gentle: Boolean, reminder: Boolean, strict: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALERT_STAGE_GENTLE] = gentle
            preferences[KEY_ALERT_STAGE_REMINDER] = reminder
            preferences[KEY_ALERT_STAGE_STRICT] = strict
        }
    }
    
    suspend fun getSavedAlertState(): Triple<Boolean, Boolean, Boolean> {
        val preferences = context.dataStore.data.first()
        val gentle = preferences[KEY_ALERT_STAGE_GENTLE] ?: false
        val reminder = preferences[KEY_ALERT_STAGE_REMINDER] ?: false
        val strict = preferences[KEY_ALERT_STAGE_STRICT] ?: false
        return Triple(gentle, reminder, strict)
    }

    suspend fun clearAlertState() {
        context.dataStore.edit { preferences ->
            preferences[KEY_ALERT_STAGE_GENTLE] = false
            preferences[KEY_ALERT_STAGE_REMINDER] = false
            preferences[KEY_ALERT_STAGE_STRICT] = false
        }
    }
}
