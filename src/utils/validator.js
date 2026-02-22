const Joi = require('joi');

const dashboardSchema = Joi.object({
    dailyUsageOfApp: Joi.object().pattern(Joi.string(), Joi.number()).required(), // App Pkg -> Limit
    totalScreenTime: Joi.number().required(), // In minutes
    focusScoreTrend: Joi.string().valid('improving', 'declining', 'stable').required(),
    blockCount: Joi.number().min(0).required(),
    userPreferences: Joi.object({
        focusGoalMin: Joi.number().default(120),
        tone: Joi.string().valid('gentle', 'strict', 'neutral').default('neutral')
    }).optional()
});

const alertSchema = Joi.object({
    alertType: Joi.string().valid('gentle', 'reminder', 'strict').required(),
    triggerReason: Joi.string().required(), // e.g., "Social Media Overuse"
    recentBehavior: Joi.object({
        appPackage: Joi.string().required(),
        usageDuringSession: Joi.number().required(), // minutes
        attemptedUnlocks: Joi.number().default(0)
    }).required(),
    userTonePreference: Joi.string().default('neutral')
});

const weeklyReviewSchema = Joi.object({
    weeklyStats: Joi.object({
        totalScreenTime: Joi.number().required(),
        mostUsedApp: Joi.string().required(),
        focusScoreAverage: Joi.number().min(0).max(100).required()
    }).required(),
    improvementDelta: Joi.number().required(), // Percentage change
    topDistractions: Joi.array().items(Joi.string()).max(5).required()
});

const validate = (schema) => (req, res, next) => {
    const { error } = schema.validate(req.body, { abortEarly: false });
    if (error) {
        return res.status(400).json({
            error: 'Validation Error',
            details: error.details.map(d => d.message)
        });
    }
    next();
};

module.exports = {
    validateRaw: (schema, data) => schema.validate(data),
    validateMiddleware: validate,
    schemas: {
        dashboard: dashboardSchema,
        alert: alertSchema,
        weekly: weeklyReviewSchema
    }
};
