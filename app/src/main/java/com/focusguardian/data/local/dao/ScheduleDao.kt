package com.focusguardian.data.local.dao

import androidx.room.*
import com.focusguardian.data.local.entity.BlockingSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM blocking_schedules")
    fun getAllSchedules(): Flow<List<BlockingSchedule>>
    
    @Query("SELECT * FROM blocking_schedules WHERE isEnabled = 1")
    suspend fun getActiveSchedules(): List<BlockingSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BlockingSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: BlockingSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: BlockingSchedule)
}
