package com.focusguardian.data.repository

import com.focusguardian.data.local.dao.ProfileDao
import com.focusguardian.data.local.dao.ScheduleDao
import com.focusguardian.data.local.entity.BlockingSchedule
import com.focusguardian.data.local.entity.BlockingProfile
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class ScheduleRepository(
    private val scheduleDao: ScheduleDao, // This is BlockingScheduleDao
    private val profileDao: ProfileDao
) {

    // Manage Schedules
    fun getAllSchedules(): Flow<List<BlockingSchedule>> = scheduleDao.getAllSchedules()

    suspend fun createSchedule(schedule: BlockingSchedule) {
        scheduleDao.insertSchedule(schedule)
    }
    
    suspend fun deleteSchedule(schedule: BlockingSchedule) {
        scheduleDao.deleteSchedule(schedule)
    }

    // Manage Profiles
    fun getAllProfiles(): Flow<List<BlockingProfile>> = profileDao.getAllProfiles()
    
    suspend fun createProfile(profile: BlockingProfile) {
        profileDao.insertProfile(profile)
    }
    
    suspend fun getProfileById(id: Int): BlockingProfile? = profileDao.getProfileById(id)

    // Blocking Logic
    suspend fun shouldBlockPackage(packageName: String): Boolean {
        // 1. Get currently active time
        val now = Calendar.getInstance()
        // Calendar.DAY_OF_WEEK: Sun=1, Mon=2...
        // My entity assumes 1=Mon..7=Sun convention usually, or we match Calendar.
        // Let's assume standard Calendar for now: Sun=1, Mon=2, etc. and UI handles conversion if needed.
        // Or better, let's document: 1=Sun, 2=Mon, ..., 7=Sat.
        
        val currentDay = now.get(Calendar.DAY_OF_WEEK) 
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute

        // 2. Get enabled schedules
        val activeSchedules = scheduleDao.getActiveSchedules()
        
        // 3. Check overlaps
        for (schedule in activeSchedules) {
            // schedule.daysOfWeek is a string "1,2,3"
            val days = schedule.daysOfWeek.split(",").mapNotNull { it.trim().toIntOrNull() }
            
            if (days.contains(currentDay)) {
                val startTime = schedule.startHour * 60 + schedule.startMinute
                val endTime = schedule.endHour * 60 + schedule.endMinute
                
                // Active range check
                val isActive = if (endTime > startTime) {
                    currentTimeInMinutes in startTime until endTime
                } else {
                    // Overnight: e.g. 23:00 to 06:00
                    currentTimeInMinutes >= startTime || currentTimeInMinutes < endTime
                }
                
                if (isActive) {
                    // 4. Check Profile
                    val profile = profileDao.getProfileById(schedule.profileId)
                    if (profile != null) {
                        val blockedApps = profile.includedApps.split(",").map { it.trim() }
                        // Check if packageName matches one of the blocked apps
                        if (blockedApps.contains(packageName)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }
}
