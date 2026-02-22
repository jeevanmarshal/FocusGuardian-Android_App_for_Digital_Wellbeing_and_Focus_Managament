package com.focusguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.focusguardian.data.local.entity.AppUsageSession

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: AppUsageSession)

    @Query("SELECT SUM(duration) FROM app_usage_session WHERE packageName = :packageName AND date = :date")
    suspend fun getTotalUsageForApp(packageName: String, date: Long): Long?

    @Query("SELECT SUM(duration) FROM app_usage_session WHERE date = :date")
    suspend fun getTotalScreenTime(date: Long): Long?

    @Query("DELETE FROM app_usage_session WHERE date < :timestamp")
    suspend fun deleteSessionsOlderThan(timestamp: Long)

    @Query("SELECT packageName, SUM(duration) as totalTime FROM app_usage_session WHERE date = :date GROUP BY packageName ORDER BY totalTime DESC LIMIT 1")
    suspend fun getMostUsedApp(date: Long): AppUsageResult?

    @Query("SELECT packageName, SUM(duration) as totalTime FROM app_usage_session WHERE date = :date GROUP BY packageName ORDER BY totalTime DESC LIMIT :limit")
    suspend fun getTopApps(date: Long, limit: Int): List<AppUsageResult>

    @Query("SELECT date, SUM(duration) as totalTime FROM app_usage_session WHERE date >= :startDate AND date <= :endDate GROUP BY date ORDER BY date ASC")
    suspend fun getDailyUsageRange(startDate: Long, endDate: Long): List<DailyUsageResult>
}

data class AppUsageResult(val packageName: String, val totalTime: Long)
data class DailyUsageResult(val date: Long, val totalTime: Long)
