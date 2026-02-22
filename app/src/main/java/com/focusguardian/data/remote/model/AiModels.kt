package com.focusguardian.data.remote.model

import com.google.gson.annotations.SerializedName

// --- Dashboard Insight ---
data class DashboardInsightRequest(
    @SerializedName("dailyUsage") val dailyUsageMinutes: Int,
    @SerializedName("violations") val violationsCount: Int,
    @SerializedName("focusScore") val focusScore: Int
)

data class DashboardInsightResponse(
    @SerializedName("insight") val insight: String
)

// --- Alert Message ---
data class AlertMessageRequest(
    @SerializedName("alertType") val alertType: String, // "gentle", "reminder", "strict"
    @SerializedName("reason") val reason: String,
    @SerializedName("behavior") val behavior: BehaviorSnapshot
)

data class BehaviorSnapshot(
    @SerializedName("rapidReopen") val rapidReopen: Boolean = false,
    @SerializedName("app") val appName: String? = null,
    @SerializedName("usageDuration") val usageDuration: Int? = 0
)

data class AlertMessageResponse(
    @SerializedName("alert") val alertMessage: String
)

// --- Weekly Review ---
data class WeeklyReviewRequest(
    @SerializedName("totalFocusTime") val totalFocusTime: Int,
    @SerializedName("totalDistractions") val totalDistractions: Int,
    @SerializedName("improvementRate") val improvementRate: Int // Percentage
)

data class WeeklyReviewResponse(
    @SerializedName("review") val reviewMessage: String
)
