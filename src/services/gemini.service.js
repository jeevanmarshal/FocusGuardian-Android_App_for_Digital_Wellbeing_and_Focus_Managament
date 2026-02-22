const { GoogleGenerativeAI } = require('@google/generative-ai');
const config = require('../config/env');
const { getSystemPrompt } = require('../utils/promptBuilder');

// Initialize Gemini
// Notes:
// - If API key is missing, we should fail gracefully or mock responses in development if needed.
// - For production, key is mandatory.
let model;
if (config.geminiApiKey) {
    const genAI = new GoogleGenerativeAI(config.geminiApiKey);
    model = genAI.getGenerativeModel({ model: "gemini-pro" });
} else {
    console.error("[GEMINI] API Key not found. AI features will respond with static fallbacks.");
}

/**
 * Clean the response to ensure valid JSON.
 * Sometimes models wrap JSON in markdown code blocks.
 */
const cleanJSON = (text) => {
    let cleaned = text.trim();
    if (cleaned.startsWith('```json')) cleaned = cleaned.replace('```json', '');
    if (cleaned.startsWith('```')) cleaned = cleaned.replace('```', '');
    if (cleaned.endsWith('```')) cleaned = cleaned.slice(0, -3);
    return cleaned.trim();
};

/**
 * Generates content using Gemini with retries and JSON validation.
 * @param {string} userPrompt - The specific prompt for the task.
 * @returns {Promise<Object>} - Parsed JSON response.
 */
const generateAIResponse = async (userPrompt) => {
    if (!model) {
        throw new Error("Gemini API Key missing");
    }

    try {
        const systemPrompt = getSystemPrompt();
        const fullPrompt = `${systemPrompt}\n\n${userPrompt}`;

        const result = await model.generateContent(fullPrompt);
        const response = await result.response;
        const text = response.text();

        try {
            return JSON.parse(cleanJSON(text));
        } catch (jsonError) {
            console.error("[GEMINI] JSON Parse Verification Failed:", text);
            // Fallback: If JSON fails, attempt to return a structured error or raw text wrapped
            return {
                raw_response: text,
                error: "Failed to parse JSON"
            };
        }
    } catch (error) {
        console.error("[GEMINI] Generation Failed:", error.message);
        throw error; // Propagate to controller for fallback handling
    }
};

module.exports = {
    generateAIResponse
};
