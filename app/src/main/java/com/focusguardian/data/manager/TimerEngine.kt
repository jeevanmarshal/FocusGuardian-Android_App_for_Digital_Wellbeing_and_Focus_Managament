package com.focusguardian.data.manager

import android.os.SystemClock
import com.focusguardian.data.local.dao.SessionDao
import com.focusguardian.data.local.entity.AppUsageSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class TimerEngine(
    private val sessionDao: SessionDao
) {
    private val timerScope = CoroutineScope(Dispatchers.IO + Job())
    
    private var currentPackage: String? = null
    private var startTime: Long = 0
    private var sessionAccumulatedTime: Long = 0
    
    // Live update of current session duration
    private val _currentSessionDuration = MutableStateFlow(0L)
    val currentSessionDuration = _currentSessionDuration.asStateFlow()

    private var tickJob: Job? = null

    fun onAppForeground(packageName: String) {
        if (currentPackage == packageName) return // Already tracking
        
        // Stop previous if exists (should have been stopped by background event, but safety)
        if (currentPackage != null) {
            onAppBackground(currentPackage!!)
        }

        currentPackage = packageName
        startTime = System.currentTimeMillis()
        sessionAccumulatedTime = 0L
        _currentSessionDuration.value = 0L
        
        startTick()
    }

    fun onAppBackground(packageName: String) {
        if (currentPackage != packageName) return // Mismatch or already stopped

        stopTick()
        
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        if (duration > 0) {
            saveSession(packageName, startTime, endTime, duration)
        }
        
        currentPackage = null
        startTime = 0
        sessionAccumulatedTime = 0
    }

    private fun startTick() {
        tickJob?.cancel()
        tickJob = timerScope.launch {
            while (true) {
                delay(1000) // 1 second tick
                if (currentPackage != null) {
                    val now = System.currentTimeMillis()
                    sessionAccumulatedTime = now - startTime
                    _currentSessionDuration.emit(sessionAccumulatedTime)
                    // TODO: Notify AlertSequenceController here if needed
                }
            }
        }
    }

    private fun stopTick() {
        tickJob?.cancel()
    }

    private fun saveSession(packageName: String, start: Long, end: Long, duration: Long) {
        timerScope.launch {
             val calendar = Calendar.getInstance()
             calendar.timeInMillis = start
             calendar.set(Calendar.HOUR_OF_DAY, 0)
             calendar.set(Calendar.MINUTE, 0)
             calendar.set(Calendar.SECOND, 0)
             calendar.set(Calendar.MILLISECOND, 0)
             val date = calendar.timeInMillis

             sessionDao.insertSession(
                 AppUsageSession(
                     packageName = packageName,
                     startTime = start,
                     endTime = end,
                     duration = duration,
                     date = date
                 )
             )
        }
    }
}
