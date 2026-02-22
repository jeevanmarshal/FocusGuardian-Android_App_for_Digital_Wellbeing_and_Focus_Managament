require('dotenv').config();

const requiredEnvVars = [
    'GEMINI_API_KEY'
];

// Check for missing environment variables
const missingVars = requiredEnvVars.filter(key => !process.env[key]);

if (missingVars.length > 0) {
    console.warn(`[WARNING] Missing environment variables: ${missingVars.join(', ')}. AI features will fail.`);
}

module.exports = {
    port: process.env.PORT || 3000,
    geminiApiKey: process.env.GEMINI_API_KEY,
    nodeEnv: process.env.NODE_ENV || 'development',
    corsOrigin: process.env.CORS_ORIGIN || '*' // Allow all for local dev/emulator
};
