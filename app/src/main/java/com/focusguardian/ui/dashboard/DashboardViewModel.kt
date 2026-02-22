package com.focusguardian.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguardian.ServiceLocator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState
    
    private var aiFetchTimestamp: Long = 0
    private val AI_REFRESH_INTERVAL = 1000 * 60 * 30 // 30 minutes


    init {
        startMonitoring()
    }
    
    private fun startMonitoring() {
        viewModelScope.launch {
            while (true) {
                updateDashboard()
                delay(1000) // Update every second for timer
            }
        }
    }

    private suspend fun updateDashboard() {
        // Ensure ServiceLocator is initialized in UI if not already, though MainActivity should have done it or App class.
        // Assuming ServiceLocator.applicationContext is safe.
        val context = try { ServiceLocator.applicationContext } catch (e: Exception) { return }

        // 1. Greeting
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 5..11 -> "Good Morning"
            in 12..17 -> "Good Afternoon"
            else -> "Good Evening"
        }

        // 2. Insights from Domain
        val inputTime = getMidnightTimestamp()
        val insight = ServiceLocator.insightProcessor.generateDailyInsight(inputTime)
        
        val totalTime = insight.totalScreenTimeMs
        val mostUsed = insight.mostUsedApp
        val score = insight.focusScore
        
        // Load Monitoring States
        val appMon = com.focusguardian.util.UserPrefs.isAppMonitoringEnabled(context)
        val webMon = com.focusguardian.util.UserPrefs.isWebsiteMonitoringEnabled(context)
        val shortsMon = com.focusguardian.util.UserPrefs.isShortsMonitoringEnabled(context)
        
        // Mode - Defaulting
        // We could fetch active profile from Repo in future
        val modeName = "Standard Mode"
        val modeColor = "#4CAF50" // Green

        _uiState.update { currentState ->
            currentState.copy(
                greeting = greeting,
                score = score,
                totalFocusTimeMinutes = totalTime / 60000,
                totalScreenTime = formatTime(totalTime), // Should reuse helper or standard util
                mostUsedApp = mostUsed,
                totalLaunches = 0, // Need to fetch launches from DB if possible
                insightMessage = if (score > 80) "Excellent discipline." else "Stay focused.",
                // focusState? Keeping IDLE for now unless we map ModeType -> FocusState
                activeModeName = modeName,
                activeModeColor = modeColor,
                remainingTime = "00:00:00",
                isAppMonitoringEnabled = appMon,
                isWebsiteMonitoringEnabled = webMon,
                isShortsMonitoringEnabled = shortsMon
            )
        }
        
        // Trigger AI Fetch if needed (e.g. periodically)
        val currentTime = System.currentTimeMillis()
        if (currentTime - aiFetchTimestamp > AI_REFRESH_INTERVAL) {
             fetchAiInsight(totalTime.toInt() / 60000, _uiState.value.blocksTriggered, score)
             aiFetchTimestamp = currentTime
        }
    }

    private fun fetchAiInsight(usageMins: Int, violations: Int, score: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAiLoading = true) }
            val result = ServiceLocator.aiInsightsRepository.fetchDashboardInsight(usageMins, violations, score)
            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { insight ->
                        currentState.copy(customAiInsight = insight, isAiLoading = false)
                    },
                    onFailure = {
                        currentState.copy(isAiLoading = false)
                    }
                )
            }
        }
    }

    private fun getMidnightTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun formatTime(ms: Long): String {
        val hours = ms / (1000 * 60 * 60)
        val minutes = (ms % (1000 * 60 * 60)) / (1000 * 60)
        return "${hours}h ${minutes}m"
    }
    
    private fun formatTimeHHMMSS(ms: Long): String {
        val hours = ms / (1000 * 60 * 60)
        val minutes = (ms % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (ms % (1000 * 60)) / 1000
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun toggleAppMonitoring(enabled: Boolean) {
        val context = ServiceLocator.applicationContext ?: return
        com.focusguardian.util.UserPrefs.setAppMonitoringEnabled(context, enabled)
        _uiState.update { it.copy(isAppMonitoringEnabled = enabled) }
    }

    fun toggleWebMonitoring(enabled: Boolean) {
         val context = ServiceLocator.applicationContext ?: return
         com.focusguardian.util.UserPrefs.setWebsiteMonitoringEnabled(context, enabled)
         _uiState.update { it.copy(isWebsiteMonitoringEnabled = enabled) }
    }

    fun toggleShortsMonitoring(enabled: Boolean) {
         val context = ServiceLocator.applicationContext ?: return
         com.focusguardian.util.UserPrefs.setShortsMonitoringEnabled(context, enabled)
         _uiState.update { it.copy(isShortsMonitoringEnabled = enabled) }
    }
}
