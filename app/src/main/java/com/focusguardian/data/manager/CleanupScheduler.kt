package com.focusguardian.data.manager

import com.focusguardian.data.repository.DataRepository
import java.util.Calendar

class CleanupScheduler(
    private val dataRepository: DataRepository
) {
    suspend fun performMidnightReset() {
        // 1. Reset Alert State (New day, new quota)
        dataRepository.clearAlertState()
        
        // 2. Perform DB Pruning (Keep 90 days)
        val ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000)
        dataRepository.deleteSessionsOlderThan(ninetyDaysAgo)
        
        // 3. Reset Daily Switch Counts (if tracked in prefs)
        // UserPrefs is static object, so we might need Context or a wrapper
        // Assuming DataRepository can handle this or we invoke via ServiceLocator if strictly needed,
        // but cleaner to keep it here if DataRepository abstracts it.
        // dataRepository.resetDailyCounters()
        
        println("FocusGuardian: Midnight reset performed.")
    }
}
