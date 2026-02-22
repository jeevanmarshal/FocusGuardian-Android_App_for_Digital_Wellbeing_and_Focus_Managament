package com.focusguardian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocking_profiles")
data class BlockingProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // e.g., "Work", "Study"
    val iconRes: String, // Resource name for icon
    val colorHex: String, // Store color as Hex string
    val includedApps: String = "", // Comma-separated package names
    val includedKeywords: String = "" // Comma-separated keywords for web
)
