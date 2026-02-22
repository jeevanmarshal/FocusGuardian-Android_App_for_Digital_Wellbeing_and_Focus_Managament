const geminiService = require('./gemini.service');
const promptBuilder = require('../utils/promptBuilder');

/**
 * Fallback responses in case AI Service is down or fails.
 */
const FALLBACKS = {
    dashboard: {
        short_insight: "Small steps lead to big focus. Keep going!",
        motivation_tag: "Stay Steady",
        confidence_score: 1.0
    },
    alert: {
        alert_title: "Focus Check",
        alert_message: "You've been distracted. Let's get back on track.",
        severity_label: "Medium"
    },
    weekly: {
        praise_section: "You tracked your time this week. Awareness is the first step.",
        improvement_suggestion: "Try to reduce screen time by 10% next week.",
        actionable_step: "Set a timer for 20 minutes of deep work."
    }
};

const generateDashboardInsight = async (data) => {
    try {
        const prompt = promptBuilder.buildDashboardPrompt(data);
        return await geminiService.generateAIResponse(prompt);
    } catch (error) {
        console.warn("[AlertService] Dashboard generation failed, using fallback.", error.message);
        return FALLBACKS.dashboard;
    }
};

const generateAlertMessage = async (data) => {
    try {
        const prompt = promptBuilder.buildAlertPrompt(data);
        return await geminiService.generateAIResponse(prompt);
    } catch (error) {
        console.warn("[AlertService] Alert generation failed, using fallback.", error.message);
        return FALLBACKS.alert;
    }
};

const generateWeeklyReview = async (data) => {
    try {
        const prompt = promptBuilder.buildWeeklyPrompt(data);
        return await geminiService.generateAIResponse(prompt);
    } catch (error) {
        console.warn("[AlertService] Weekly review generation failed, using fallback.", error.message);
        return FALLBACKS.weekly;
    }
};

module.exports = {
    generateDashboardInsight,
    generateAlertMessage,
    generateWeeklyReview
};
