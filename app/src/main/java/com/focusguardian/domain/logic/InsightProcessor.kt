package com.focusguardian.domain.logic

import com.focusguardian.data.repository.DataRepository
import kotlinx.coroutines.flow.first

class InsightProcessor(
    private val dataRepository: DataRepository
) {

    data class DailyInsight(
        val totalScreenTimeMs: Long,
        val mostUsedApp: String,
        val focusScore: Int
    )

    suspend fun generateDailyInsight(dateTimestamp: Long): DailyInsight {
        val usageDao = dataRepository.getUsageDao()
        val appUsageList = usageDao.getAppUsageForDate(dateTimestamp).first()

        var totalTime = 0L
        var maxTime = 0L
        var topPackage = "None"

        appUsageList.forEach { usage ->
            totalTime += usage.usageDurationMillis
            if (usage.usageDurationMillis > maxTime) {
                maxTime = usage.usageDurationMillis
                topPackage = usage.packageName
            }
        }

        // Simplistic Focus Score: 100 - (Total hours * 5)
        val usageHours = totalTime / (1000 * 60 * 60)
        val score = (100 - (usageHours * 5)).toInt().coerceIn(0, 100)

        return DailyInsight(
            totalScreenTimeMs = totalTime,
            mostUsedApp = topPackage,
            focusScore = score
        )
    }
}
