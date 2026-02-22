package com.focusguardian.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguardian.ServiceLocator
import com.focusguardian.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

data class AnalyticsUiState(
    val selectedDate: Calendar = Calendar.getInstance(),
    val usageList: List<AppUsageEntity> = emptyList(),
    val isLoading: Boolean = false
)

class AnalyticsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState

    init {
        loadData(Calendar.getInstance())
    }

    fun onDateSelected(calendar: Calendar) {
        _uiState.value = _uiState.value.copy(selectedDate = calendar)
        loadData(calendar)
    }

    private fun loadData(calendar: Calendar) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val midnight = getMidnight(calendar)
            val usageDao = ServiceLocator.dataRepository.getUsageDao()
            val usage = usageDao.getDailyUsage(midnight)
            _uiState.value = _uiState.value.copy(
                usageList = usage,
                isLoading = false
            )
        }
    }

    private fun getMidnight(calendar: Calendar): Long {
        val c = calendar.clone() as Calendar
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }
}
