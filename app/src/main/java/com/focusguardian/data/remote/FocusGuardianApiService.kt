package com.focusguardian.data.remote

import com.focusguardian.data.remote.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FocusGuardianApiService {

    @POST("ai/dashboard-insight")
    suspend fun getDashboardInsight(
        @Body request: DashboardInsightRequest
    ): Response<DashboardInsightResponse>

    @POST("ai/alert-message")
    suspend fun getAlertMessage(
        @Body request: AlertMessageRequest
    ): Response<AlertMessageResponse>

    @POST("ai/weekly-review")
    suspend fun getWeeklyReview(
        @Body request: WeeklyReviewRequest
    ): Response<WeeklyReviewResponse>
}
