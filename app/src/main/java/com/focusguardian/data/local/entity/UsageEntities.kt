package com.focusguardian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_stats")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val date: Long, // Midnight timestamp
    val usageDurationMillis: Long,
    val openCount: Int
)

@Entity(tableName = "website_usage_stats")
data class WebsiteUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val domain: String,
    val date: Long, // Midnight timestamp
    val usageDurationMillis: Long,
    val visitCount: Int
)
