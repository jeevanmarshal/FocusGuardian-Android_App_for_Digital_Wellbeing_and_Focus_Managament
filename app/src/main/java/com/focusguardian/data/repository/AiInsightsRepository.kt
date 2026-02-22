package com.focusguardian.data.repository

import android.util.Log
import com.focusguardian.data.remote.FocusGuardianApiService
import com.focusguardian.data.remote.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AiInsightsRepository(
    private val apiService: FocusGuardianApiService
) {

    suspend fun fetchDashboardInsight(
        dailyUsage: Int, 
        violations: Int, 
        focusScore: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = DashboardInsightRequest(dailyUsage, violations, focusScore)
            val response = apiService.getDashboardInsight(request)
            
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.insight)
            } else {
                Result.failure(Exception("API Error: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e("AiInsightsRepo", "Error fetching dashboard insight", e)
            Result.failure(e)
        }
    }

    suspend fun fetchAlertMessage(
        type: String,
        reason: String,
        isRapidReopen: Boolean,
        appName: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val behavior = BehaviorSnapshot(rapidReopen = isRapidReopen, appName = appName)
            val request = AlertMessageRequest(type, reason, behavior)
            val response = apiService.getAlertMessage(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.alertMessage)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AiInsightsRepo", "Error fetching alert message", e)
            Result.failure(e)
        }
    }

    suspend fun fetchWeeklyReview(
        focusTime: Int,
        distractions: Int,
        improvement: Int
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = WeeklyReviewRequest(focusTime, distractions, improvement)
            val response = apiService.getWeeklyReview(request)

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.reviewMessage)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e("AiInsightsRepo", "Error fetching weekly review", e)
            Result.failure(e)
        }
    }
}
