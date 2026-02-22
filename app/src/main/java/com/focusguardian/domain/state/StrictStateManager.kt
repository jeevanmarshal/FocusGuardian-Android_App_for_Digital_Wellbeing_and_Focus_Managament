package com.focusguardian.domain.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StrictStateManager {

    private val _isStrictModeActive = MutableStateFlow(false)
    val isStrictModeActive: StateFlow<Boolean> = _isStrictModeActive.asStateFlow()

    private var activationTime: Long = 0
    private var activeReason: String = ""

    fun setActive(isActive: Boolean, reason: String = "") {
        _isStrictModeActive.value = isActive
        if (isActive) {
            activationTime = System.currentTimeMillis()
            activeReason = reason
        } else {
            activationTime = 0
            activeReason = ""
        }
    }

    fun getActivationReason(): String = activeReason
    
    // Safety check: Don't allow strict mode to be active for > 24 hours without reset
    fun isValid(): Boolean {
        if (!_isStrictModeActive.value) return false
        val now = System.currentTimeMillis()
        if (now - activationTime > 24 * 60 * 60 * 1000L) {
            // Safety release
            setActive(false, "Safety Timeout")
            return false
        }
        return true
    }
}
