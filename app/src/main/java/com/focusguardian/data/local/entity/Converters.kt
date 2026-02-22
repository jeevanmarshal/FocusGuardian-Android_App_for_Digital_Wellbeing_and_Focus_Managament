package com.focusguardian.data.local.entity

import androidx.room.TypeConverter
import com.focusguardian.model.AlertStage
import com.focusguardian.model.AppCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    @TypeConverter
    fun fromAppCategory(category: AppCategory?): String {
        return category?.name ?: AppCategory.OTHER.name
    }

    @TypeConverter
    fun toAppCategory(value: String?): AppCategory {
        return try {
            if (value != null) AppCategory.valueOf(value) else AppCategory.OTHER
        } catch (e: Exception) {
            AppCategory.OTHER
        }
    }

    @TypeConverter
    fun fromAlertStage(stage: AlertStage?): String {
        return stage?.name ?: AlertStage.NONE.name
    }

    @TypeConverter
    fun toAlertStage(value: String?): AlertStage {
        return try {
            if (value != null) AlertStage.valueOf(value) else AlertStage.NONE
        } catch (e: Exception) {
            AlertStage.NONE
        }
    }
    
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return try {
            Gson().fromJson(value, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // DayOfWeek Set Converter for Schedules
    @TypeConverter
    fun fromDayOfWeekSet(value: Set<java.time.DayOfWeek>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toDayOfWeekSet(value: String?): Set<java.time.DayOfWeek> {
        val type = object : TypeToken<Set<java.time.DayOfWeek>>() {}.type
        return try {
            Gson().fromJson(value, type)
        } catch (e: Exception) {
            emptySet()
        }
    }
    @TypeConverter
    fun fromIntList(value: List<Int>?): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String?): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return try {
            Gson().fromJson(value, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
