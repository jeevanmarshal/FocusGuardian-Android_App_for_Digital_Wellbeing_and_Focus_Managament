package com.focusguardian.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.focusguardian.data.local.dao.ScheduleDao
import com.focusguardian.data.local.dao.SessionDao
import com.focusguardian.data.local.dao.UsageDao
import com.focusguardian.data.local.entity.AppUsageEntity
import com.focusguardian.data.local.entity.AppUsageSession
import com.focusguardian.data.local.entity.Converters
import com.focusguardian.data.local.entity.ScheduleEntity
import com.focusguardian.data.local.entity.WebsiteUsageEntity

@Database(entities = [
    AppUsageEntity::class, 
    WebsiteUsageEntity::class, 
    ScheduleEntity::class, 
    AppUsageSession::class,
    com.focusguardian.data.local.entity.BlockingProfile::class,
    com.focusguardian.data.local.entity.BlockingSchedule::class
], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageDao(): UsageDao
    // abstract fun scheduleDao(): ScheduleDao // Using the old one for now if needed, or replace
    // abstract fun sessionDao(): SessionDao
    
    // I need to provide access to ALL DAOs
    // abstract fun legacyScheduleDao(): ScheduleDao // Removed to fix duplicated DAO error
    
    abstract fun sessionDao(): SessionDao
    abstract fun profileDao(): com.focusguardian.data.local.dao.ProfileDao
    abstract fun blockingScheduleDao(): com.focusguardian.data.local.dao.ScheduleDao // This is the new one I wrote?
}
