package com.focusguardian.domain.state

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// @Singleton (Manual Singleton via ServiceLocator)
class BlockStateManager {

    private val _currentBlockedTarget = MutableStateFlow<String?>(null)
    val currentBlockedTarget: StateFlow<String?> = _currentBlockedTarget.asStateFlow()

    private var isOverlayShowing = false

    fun isBlocked(target: String): Boolean {
        return _currentBlockedTarget.value == target && isOverlayShowing
    }

    fun markBlocked(target: String) {
        if (_currentBlockedTarget.value != target) {
            _currentBlockedTarget.value = target
            isOverlayShowing = true
        }
    }

    fun clearBlock() {
        _currentBlockedTarget.value = null
        isOverlayShowing = false
    }
    
    fun isOverlayActive(): Boolean = isOverlayShowing
}
