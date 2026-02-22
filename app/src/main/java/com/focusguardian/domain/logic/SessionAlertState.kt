package com.focusguardian.domain.logic

data class SessionAlertState(
    val gentleShown: Boolean = false,
    val reminderShown: Boolean = false,
    val strictShown: Boolean = false
)
