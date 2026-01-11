const alertService = require('../services/alert.service');

const getDashboardInsight = async (req, res) => {
    try {
        // Data is already validated by middleware before reaching here
        const insight = await alertService.generateDashboardInsight(req.body);
        res.json(insight);
    } catch (error) {
        console.error("Controller Error:", error);
        res.status(500).json({ error: "Internal Server Error" });
    }
};

const getAlertMessage = async (req, res) => {
    try {
        const message = await alertService.generateAlertMessage(req.body);
        res.json(message);
    } catch (error) {
        console.error("Controller Error:", error);
        res.status(500).json({ error: "Internal Server Error" });
    }
};

const getWeeklyReview = async (req, res) => {
    try {
        const review = await alertService.generateWeeklyReview(req.body);
        res.json(review);
    } catch (error) {
        console.error("Controller Error:", error);
        res.status(500).json({ error: "Internal Server Error" });
    }
};

module.exports = {
    getDashboardInsight,
    getAlertMessage,
    getWeeklyReview
};
