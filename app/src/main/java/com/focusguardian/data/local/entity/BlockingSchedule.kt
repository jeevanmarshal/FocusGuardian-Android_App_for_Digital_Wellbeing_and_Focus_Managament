package com.focusguardian.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "blocking_schedules",
    foreignKeys = [
        ForeignKey(
            entity = BlockingProfile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class BlockingSchedule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val name: String, // "Weekday Mornings"
    val daysOfWeek: String, // "1,2,3,4,5" (Mon=1..Sun=7)
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isEnabled: Boolean = true
)
