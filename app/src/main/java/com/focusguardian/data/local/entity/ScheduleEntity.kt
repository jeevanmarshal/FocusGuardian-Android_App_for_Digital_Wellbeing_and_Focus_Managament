package com.focusguardian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val startTimeHour: Int,
    val startTimeMinute: Int,
    val endTimeHour: Int,
    val endTimeMinute: Int,
    val daysOfWeek: List<Int>, // 1=Sunday, 2=Monday, ... 7=Saturday
    val isEnabled: Boolean = true,
    val blockedPackageNames: List<String> = emptyList()
)

// Converters removed to avoid duplication
