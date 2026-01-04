package com.focusguardian.logic

import android.content.Context
import com.focusguardian.util.UserPrefs

/**
 * Rules-based engine to classify cognitive state based on behavior and intent.
 */
object CognitiveStateClassifier {

    fun classify(context: Context): CognitiveState {
        val avgSwitchInterval = CognitiveFeatureExtractor.getAverageSwitchInterval()
        val shortSessions = CognitiveFeatureExtractor.getShortSessionCount()
        val isLateNight = CognitiveFeatureExtractor.isLateNightUsage()
        val globalIntent = UserPrefs.getGlobalIntent(context)
        
        // Rules for classification
        return when {
            // High restlessness: rapid switching and many short sessions
            avgSwitchInterval < 60000 && shortSessions > 5 -> {
                if (globalIntent == "STUDY" || globalIntent == "WORK") {
                    CognitiveState.STRESSED
                } else {
                    CognitiveState.DISTRACTED
                }
            }
            // Continuous usage in late night
            isLateNight && shortSessions < 2 -> CognitiveState.OVERLOADED
            // Moderate switching during focused intent
            avgSwitchInterval < 120000 && (globalIntent == "STUDY" || globalIntent == "WORK") -> CognitiveState.DISTRACTED
            // Default calm state
            else -> CognitiveState.CALM
        }
    }
}
