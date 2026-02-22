package com.focusguardian.data.manager

import com.focusguardian.data.local.dao.SessionDao
import com.focusguardian.data.local.dao.UsageDao
import com.focusguardian.data.repository.ScheduleRepository

class AnalyticsCollector(
    private val sessionDao: SessionDao,
    private val usageDao: UsageDao,
    private val scheduleRepository: ScheduleRepository
) {
    // Collects raw data on demand for the processor
    
    suspend fun getDailyUsage(date: Long): Long {
         return sessionDao.getTotalScreenTime(date) ?: 0L
    }
    
    suspend fun getUsageForPackage(packageName: String, date: Long): Long {
        return sessionDao.getTotalUsageForApp(packageName, date) ?: 0L
    }
    
    suspend fun getMostUsedApp(date: Long): com.focusguardian.data.local.dao.AppUsageResult? {
        return sessionDao.getMostUsedApp(date)
    }
    
    suspend fun getTopApps(date: Long, limit: Int): List<com.focusguardian.data.local.dao.AppUsageResult> {
        return sessionDao.getTopApps(date, limit)
    }

    suspend fun getUsageRange(start: Long, end: Long): List<com.focusguardian.data.local.dao.DailyUsageResult> {
        return sessionDao.getDailyUsageRange(start, end)
    }
}
