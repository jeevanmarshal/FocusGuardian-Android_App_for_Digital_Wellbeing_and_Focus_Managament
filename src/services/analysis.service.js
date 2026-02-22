/**
 * Usage Analysis Service
 * Implements rule-based logic for behavioral signals and risk flags.
 */

/**
 * calculateRiskLevel
 * Determines risk based on screen time and block counts.
 */
const calculateRiskLevel = (screenTime, blockCount) => {
    if (screenTime > 300 || blockCount > 10) return 'HIGH';
    if (screenTime > 180 || blockCount > 5) return 'MEDIUM';
    return 'LOW';
};

/**
 * analyzeBehavior
 * Returns additional context flags for the AI.
 */
const analyzeBehavior = (usageData) => {
    const risk = calculateRiskLevel(usageData.totalScreenTime, usageData.blockCount);

    // Detect Time-of-day patterns (simple heuristic)
    // Assuming backend receives local time context or infers it. 
    // For now, we rely on input stats.

    const signals = [];
    if (usageData.focusScoreTrend === 'declining') signals.push('needs_motivation');
    if (risk === 'HIGH') signals.push('needs_intervention');

    return {
        riskLevel: risk,
        signals: signals
    };
};

module.exports = {
    calculateRiskLevel,
    analyzeBehavior
};
