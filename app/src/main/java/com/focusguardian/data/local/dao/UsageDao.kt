package com.focusguardian.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.focusguardian.data.local.entity.AppUsageEntity
import com.focusguardian.data.local.entity.WebsiteUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageDao {
    @Query("SELECT * FROM app_usage_stats WHERE date = :date")
    fun getAppUsageForDate(date: Long): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage_stats WHERE date = :date")
    suspend fun getDailyUsage(date: Long): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage_stats WHERE packageName = :packageName AND date = :date")
    suspend fun getAppUsage(packageName: String, date: Long): AppUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAppUsage(usage: AppUsageEntity)

    @Query("SELECT * FROM website_usage_stats WHERE date = :date")
    fun getWebsiteUsageForDate(date: Long): Flow<List<WebsiteUsageEntity>>

    @Query("SELECT * FROM website_usage_stats WHERE domain = :domain AND date = :date")
    suspend fun getWebsiteUsage(domain: String, date: Long): WebsiteUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWebsiteUsage(usage: WebsiteUsageEntity)
}
