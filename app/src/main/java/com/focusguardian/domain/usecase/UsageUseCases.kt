package com.focusguardian.domain.usecase

import com.focusguardian.data.local.entity.AppUsageEntity
import com.focusguardian.data.repository.UsageRepository
import kotlinx.coroutines.flow.Flow

class TrackAppUsageUseCase(
    private val repository: UsageRepository
) {
    suspend operator fun invoke(packageName: String, timeAddedMillis: Long, isNewLaunch: Boolean) {
        repository.updateAppUsage(packageName, timeAddedMillis, isNewLaunch)
    }
}

class GetTodayAppUsageUseCase(
    private val repository: UsageRepository
) {
    operator fun invoke(): Flow<List<AppUsageEntity>> {
        return repository.getTodayAppUsage()
    }
}
