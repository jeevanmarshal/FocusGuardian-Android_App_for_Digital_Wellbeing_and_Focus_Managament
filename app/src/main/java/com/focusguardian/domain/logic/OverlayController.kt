package com.focusguardian.domain.logic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OverlayController {

    sealed class OverlayState {
        object None : OverlayState()
        data class Gentle(val targetName: String, val message: String, val icon: String?) : OverlayState()
        data class Reminder(val targetName: String, val message: String, val icon: String?) : OverlayState()
        data class Strict(val targetName: String, val message: String, val durationSeconds: Long) : OverlayState()
        data class AccessBlocked(val targetName: String, val pendingTask: String) : OverlayState()
    }

    private val _overlayState = MutableStateFlow<OverlayState>(OverlayState.None)
    val overlayState: StateFlow<OverlayState> = _overlayState

    fun showGentleAlert(name: String, msg: String, icon: String? = null) {
        _overlayState.value = OverlayState.Gentle(name, msg, icon)
    }

    fun showReminderAlert(name: String, msg: String, icon: String? = null) {
        _overlayState.value = OverlayState.Reminder(name, msg, icon)
    }

    fun showStrictAlert(name: String, msg: String, duration: Long) {
        _overlayState.value = OverlayState.Strict(name, msg, duration)
    }

    fun showAccessBlocked(name: String, task: String) {
        _overlayState.value = OverlayState.AccessBlocked(name, task)
    }

    fun hideOverlay() {
        _overlayState.value = OverlayState.None
    }
}
