package com.focusguardian.data.repository

import com.focusguardian.data.local.LocalStorageManager
import com.focusguardian.data.local.dao.ScheduleDao
import com.focusguardian.data.local.dao.SessionDao
import com.focusguardian.data.local.dao.UsageDao
import com.focusguardian.domain.logic.SessionAlertState

class DataRepository(
    private val localStorageManager: LocalStorageManager,
    private val scheduleDao: ScheduleDao,
    private val sessionDao: SessionDao,
    private val usageDao: UsageDao
) {

    // Proxy Strict Mode Persistence
    suspend fun saveStrictMode(isActive: Boolean, reason: String) {
        localStorageManager.saveStrictModeState(isActive, if (isActive) System.currentTimeMillis() else 0, reason)
    }
    
    // Alert Persistence (Phase 9 Re-integration)
    suspend fun saveAlertState(state: SessionAlertState) {
        localStorageManager.saveAlertState(state.gentleShown, state.reminderShown, state.strictShown)
    }

    suspend fun restoreAlertState(): SessionAlertState {
        val (g, r, s) = localStorageManager.getSavedAlertState()
        return SessionAlertState(g, r, s)
    }

    suspend fun clearAlertState() {
        localStorageManager.clearAlertState()
    }

    suspend fun deleteSessionsOlderThan(timestamp: Long) {
        sessionDao.deleteSessionsOlderThan(timestamp)
    }

    // Access to DAOs for other components
    fun getUsageDao() = usageDao
}
