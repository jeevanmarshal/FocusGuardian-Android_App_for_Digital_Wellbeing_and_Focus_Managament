package com.focusguardian.model

import android.graphics.drawable.Drawable
import com.focusguardian.model.AppCategory

data class InstalledApp(
    val packageName: String,
    val label: String,
    val category: AppCategory,
    val icon: Drawable?, // Nullable if not loaded yet
    val isMonitored: Boolean,
    val isGentleEnabled: Boolean,
    val isReminderEnabled: Boolean,
    val isStrictEnabled: Boolean
)
