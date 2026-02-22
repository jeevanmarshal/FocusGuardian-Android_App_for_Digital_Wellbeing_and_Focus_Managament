package com.focusguardian.model.mode

import com.focusguardian.model.AlertStage
import com.focusguardian.model.AppCategory

/**
 * COMPONENT M-0 — MODE DEFINITION MODEL
 * Purpose: Authoritative specification for a Mode.
 * Mode objects must be serializable and persistable.
 */

enum class ModeType {
    FOCUS, WORK, STUDY, SLEEP, CUSTOM, NORMAL
}

enum class ActivationType {
    MANUAL, SCHEDULED, TEMPORARY
}

data class ModeDefinition(
    val modeId: String,
    val modeName: String,
    val iconRef: String, // Resource name or URI
    val colorTheme: String, // Hex code or Theme Token
    val priority: Int, // Higher number = Higher priority
    val activationType: ActivationType,
    val type: ModeType,
    
    // Timing (Null if manual/indefinite)
    val startTime: Long? = null,
    val endTime: Long? = null,
    val sessionExpiry: Long? = null,
    
    // Enforcement Profile
    val enforcementProfile: EnforcementProfile,
    
    // Analytics
    val analyticsMetadata: AnalyticsMetadata
)

data class EnforcementProfile(
    // Category specific rules (e.g. Social -> STRICT)
    val categoryRules: Map<AppCategory, CategoryRule>,
    
    // Global defaults for this mode
    val defaultAlertStage: AlertStage,
    val defaultBlockStrength: Int, // 0-100? Or specific levels
    
    // Shorts/Reels Policy
    val shortsPolicy: ShortsPolicy,
    
    // Overrides
    val allowManualOverride: Boolean,
    val overridePenaltyMultiplier: Float
)

data class CategoryRule(
    val enforcementLevel: EnforcementLevel,
    val allowedAlertStages: List<AlertStage> 
)

enum class EnforcementLevel {
    ALLOWED,
    GENTLE,
    REMINDER,
    STRICT,
    BLOCKED // Hard block (Sleep mode)
}

enum class ShortsPolicy {
    ALLOW,
    ALERT_ONLY,
    BLOCK_IMMEDIATE,
    STRICT_BLOCK // Forces exit
}

data class AnalyticsMetadata(
    val scoreMultiplier: Float,
    val violationSeverity: String, // LOW, MEDIUM, HIGH, CRITICAL
    val overrideSeverity: String
)
