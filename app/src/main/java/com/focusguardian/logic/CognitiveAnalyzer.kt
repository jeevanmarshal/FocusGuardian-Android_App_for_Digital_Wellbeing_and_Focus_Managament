package com.focusguardian.logic

import android.content.Context
import com.focusguardian.util.UserPrefs
import com.focusguardian.util.AnalyticsStore

enum class CognitiveState {
    CALM, DISTRACTED, OVERLOADED, STRESSED
}

object CognitiveAnalyzer {

    fun getCognitiveLoad(context: Context): CognitiveState {
        return CognitiveStateClassifier.classify(context)
    }

    fun getAIInsight(context: Context): String {
        val load = getCognitiveLoad(context)
        return AIInstructionEngine.generateInstruction(context, load)
    }
}
