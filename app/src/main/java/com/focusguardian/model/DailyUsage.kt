package com.focusguardian.model

data class DailyUsage(
    val date: String,
    val focusedMinutes: Int,
    val distractedMinutes: Int
) {
    fun focusScore(): Int {
        val total = focusedMinutes + distractedMinutes
        return if (total == 0) 0 else (focusedMinutes * 100) / total
    }
}
