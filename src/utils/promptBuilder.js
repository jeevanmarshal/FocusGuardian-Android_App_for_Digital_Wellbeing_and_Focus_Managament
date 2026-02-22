/**
 * Constructs the system prompt for Gemini.
 */
const getSystemPrompt = () => `
You are a focus-coaching assistant inside a productivity app called Focus Guardian.
Your role is to encourage, guide, and gently correct user behavior based on app usage patterns.
Be calm, respectful, non-judgmental, and practical.
Never shame, threaten, or guilt the user.
Always promote self-control and awareness.

RULES:
1. Output STRICT JSON only. No markdown formatting (no \`\`\`).
2. No emojis unless explicitly requested.
3. No medical or mental health claims.
4. Keep messages concise.
`;

/**
 * Builds the prompt for Dashboard Insights
 */
const buildDashboardPrompt = (data) => {
    return `
    USER CONTEXT:
    - Screen Time: ${data.totalScreenTime} minutes
    - Focus Trend: ${data.focusScoreTrend}
    - Blocks Today: ${data.blockCount}
    - Goal: ${data.userPreferences?.focusGoalMin || 120} minutes

    TASK:
    Generate a dashboard insight card content.
    Max 60 words.

    OUTPUT JSON FORMAT:
    {
        "short_insight": "string",
        "motivation_tag": "string (1-3 words)",
        "confidence_score": number (0-1)
    }
    `;
};

/**
 * Builds the prompt for Alerts
 */
const buildAlertPrompt = (data) => {
    const toneInstruction = data.alertType === 'strict'
        ? "Be firm but supportive. Emphasize the commitment."
        : "Be gentle and inquiring. Ask if this is intentional.";

    return `
    ALERT CONTEXT:
    - Type: ${data.alertType.toUpperCase()}
    - Trigger: ${data.triggerReason}
    - App: ${data.recentBehavior.appPackage}
    - Session: ${data.recentBehavior.usageDuringSession} mins
    
    TONE: ${toneInstruction}

    TASK:
    Generate an alert message.
    Max 40 words.

    OUTPUT JSON FORMAT:
    {
        "alert_title": "string",
        "alert_message": "string",
        "severity_label": "string (Low/Medium/High)"
    }
    `;
};

/**
 * Builds the prompt for Weekly Review
 */
const buildWeeklyPrompt = (data) => {
    return `
    WEEKLY STATS:
    - Total Time: ${data.weeklyStats.totalScreenTime} mins
    - Focus Score Avg: ${data.weeklyStats.focusScoreAverage}
    - Improvement: ${data.improvementDelta}%
    - Top Distractions: ${data.topDistractions.join(', ')}

    TASK:
    Generate a weekly review.

    OUTPUT JSON FORMAT:
    {
        "praise_section": "string",
        "improvement_suggestion": "string",
        "actionable_step": "string"
    }
    `;
};

module.exports = {
    getSystemPrompt,
    buildDashboardPrompt,
    buildAlertPrompt,
    buildWeeklyPrompt
};
