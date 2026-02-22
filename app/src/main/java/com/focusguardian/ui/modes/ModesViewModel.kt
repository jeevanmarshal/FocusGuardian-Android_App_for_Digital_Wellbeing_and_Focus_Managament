package com.focusguardian.ui.modes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguardian.ServiceLocator
import com.focusguardian.domain.logic.ModeManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ModesViewModel : ViewModel() {

    // Pull from ServiceLocator (Manual DI)
    private val modeManager: ModeManager
        get() = ServiceLocator.modeManager

    val currentMode: StateFlow<ModeManager.ModeType> = modeManager.currentMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ModeManager.ModeType.FOCUS)

    fun activateMode(mode: ModeManager.ModeType) {
        modeManager.setMode(mode)
    }
}
