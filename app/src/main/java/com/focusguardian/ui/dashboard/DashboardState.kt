package com.focusguardian.ui.dashboard

data class DashboardState(
    val greeting: String = "Welcome Back",
    
    // Mode Info
    val activeModeName: String = "Effective Mode",
    val activeModeColor: String = "#888888",
    val activeModeIcon: String = "ic_mode_normal",
    
    val focusState: FocusState = FocusState.IDLE,
    val remainingTime: String = "00:00:00",
    val score: Int = 100,
    val totalFocusTimeMinutes: Long = 0,
    val blocksTriggered: Int = 0,
    val streakDays: Int = 0,
    val insightMessage: String = "Ready to focus?",
    val nextBreakTime: String = "--:--",
    val customAiInsight: String? = null,
    val isAiLoading: Boolean = false,
    val totalScreenTime: String = "0h 0m",
    val mostUsedApp: String = "None",
    val totalLaunches: Int = 0,
    
    // Monitoring States
    val isAppMonitoringEnabled: Boolean = true,
    val isWebsiteMonitoringEnabled: Boolean = true,
    val isShortsMonitoringEnabled: Boolean = true
)
