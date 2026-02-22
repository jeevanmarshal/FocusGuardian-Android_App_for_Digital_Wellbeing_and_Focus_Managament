package com.focusguardian.domain.logic

import android.content.Context
import com.focusguardian.util.UserPrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ModeManager(private val context: Context) {

    enum class ModeType {
        FOCUS, WORK, STUDY, SLEEP, CUSTOM
    }

    private val _currentMode = MutableStateFlow(loadInitialMode())
    val currentMode: StateFlow<ModeType> = _currentMode

    fun setMode(mode: ModeType) {
        _currentMode.value = mode
        UserPrefs.setCurrentModeType(context, mode.name)
    }

    fun getCurrentMode(): ModeType {
        return _currentMode.value
    }
    
    private fun loadInitialMode(): ModeType {
        return try {
            val name = UserPrefs.getCurrentModeType(context)
            ModeType.valueOf(name)
        } catch (e: Exception) {
            ModeType.FOCUS
        }
    }
}
