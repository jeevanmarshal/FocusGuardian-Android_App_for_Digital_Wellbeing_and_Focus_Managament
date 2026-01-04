package com.focusguardian.logic

import android.content.Context
import com.focusguardian.util.UserPrefs

/**
 * AI Guidance layer that generates explainable, ethical instructions.
 */
object AIInstructionEngine {

    fun generateInstruction(context: Context, state: CognitiveState): String {
        val intent = UserPrefs.getGlobalIntent(context)
        val avgInterval = CognitiveFeatureExtractor.getAverageSwitchInterval()
        
        return when (state) {
            CognitiveState.STRESSED -> {
                if (intent == "STUDY" || intent == "WORK") {
                    "Frequent app switching detected during your $intent session. Your cognitive load is high. Try a 5-minute breathing break to reset."
                } else {
                    "You seem restless. Consider stepping away from the screen for a moment."
                }
            }
            CognitiveState.OVERLOADED -> {
                "You've been active for a long time, even late at night. Your focus efficiency might be dropping. Time for some rest?"
            }
            CognitiveState.DISTRACTED -> {
                val intervalSec = avgInterval / 1000
                "Minor distractions detected (switching every $intervalSec seconds). Re-focus on your $intent intent to stay productive."
            }
            CognitiveState.CALM -> {
                "Excellent focus patterns. You are in a high-flow state for your $intent session. Keep it up!"
            }
        }
    }
}
