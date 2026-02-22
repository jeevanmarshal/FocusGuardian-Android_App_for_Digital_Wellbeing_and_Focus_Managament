package com.focusguardian.data.repository

import com.focusguardian.data.local.dao.UsageDao
import com.focusguardian.data.local.entity.AppUsageEntity
import com.focusguardian.data.local.entity.WebsiteUsageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class UsageRepository(
    private val usageDao: UsageDao
) {

    private fun getTodayMidnight(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    suspend fun updateAppUsage(packageName: String, timeAddedMillis: Long, incrementOpenCount: Boolean) {
        val today = getTodayMidnight()
        val currentStats = usageDao.getAppUsage(packageName, today)
        
        val newStats = if (currentStats != null) {
            currentStats.copy(
                usageDurationMillis = currentStats.usageDurationMillis + timeAddedMillis,
                openCount = if (incrementOpenCount) currentStats.openCount + 1 else currentStats.openCount
            )
        } else {
            AppUsageEntity(
                packageName = packageName,
                date = today,
                usageDurationMillis = timeAddedMillis,
                openCount = if (incrementOpenCount) 1 else 0
            )
        }
        usageDao.insertOrUpdateAppUsage(newStats)
    }

    suspend fun updateWebsiteUsage(domain: String, timeAddedMillis: Long) {
        val today = getTodayMidnight()
        val currentStats = usageDao.getWebsiteUsage(domain, today)

        val newStats = if (currentStats != null) {
            currentStats.copy(
                usageDurationMillis = currentStats.usageDurationMillis + timeAddedMillis,
                visitCount = currentStats.visitCount + 1 
            )
        } else {
            WebsiteUsageEntity(
                domain = domain,
                date = today,
                usageDurationMillis = timeAddedMillis,
                visitCount = 1
            )
        }
        usageDao.insertOrUpdateWebsiteUsage(newStats)
    }

    fun getTodayAppUsage(): Flow<List<AppUsageEntity>> {
        return usageDao.getAppUsageForDate(getTodayMidnight())
    }
}
