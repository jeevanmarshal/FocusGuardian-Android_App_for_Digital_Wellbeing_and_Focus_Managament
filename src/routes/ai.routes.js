const express = require('express');
const router = express.Router();
const controller = require('../controllers/ai.controller');
const { validateMiddleware, schemas } = require('../utils/validator');

router.post('/dashboard-insight', validateMiddleware(schemas.dashboard), controller.getDashboardInsight);
router.post('/alert-message', validateMiddleware(schemas.alert), controller.getAlertMessage);
router.post('/weekly-review', validateMiddleware(schemas.weekly), controller.getWeeklyReview);

module.exports = router;
