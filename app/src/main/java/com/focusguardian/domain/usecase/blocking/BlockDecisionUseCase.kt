package com.focusguardian.domain.usecase.blocking

enum class BlockReason {
    SCHEDULE,
    LIMIT,
    STRICT,
    FOCUS,
    NONE
}

data class BlockDecisionResult(
    val shouldBlock: Boolean,
    val reason: BlockReason,
    val message: String = ""
)

class BlockDecisionUseCase {
    
    // In a real scenario, inject repositories here (Rules, Limits, Schedules)
    // val ruleRepository: RuleRepository
    
    suspend fun checkBlock(target: String, isWebsite: Boolean): BlockDecisionResult {
        // 1. Check Strict Mode (Priority)
        // if (strictModeRepo.isActive()) return BlockDecisionResult(true, BlockReason.STRICT, "Strict Mode Active")

        // 2. Check Focus Session
        // if (focusSessionRepo.isActive()) return BlockDecisionResult(true, BlockReason.FOCUS, "Focus Session Active")

        // 3. Check Schedule
        // if (scheduleRepo.isScheduled(target)) return BlockDecisionResult(true, BlockReason.SCHEDULE, "Scheduled Block")

        // 4. Check Limits
        // if (limitRepo.isLimitExceeded(target)) return BlockDecisionResult(true, BlockReason.LIMIT, "Daily Limit Reached")

        return BlockDecisionResult(false, BlockReason.NONE)
    }
}
