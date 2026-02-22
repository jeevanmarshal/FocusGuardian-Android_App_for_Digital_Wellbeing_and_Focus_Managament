package com.focusguardian.logic

import org.junit.Test
import org.junit.Assert.*

class CognitiveStateClassifierTest {

    @Test
    fun testCalmState() {
        // Low switches, low short sessions, not late night
        val state = CognitiveStateClassifier.classify(
            switches = 2,
            shortSessions = 1,
            isLateNight = false,
            userIntent = "STUDY"
        )
        assertEquals(CognitiveState.CALM, state)
    }

    @Test
    fun testStressedState() {
        // High switches, high short sessions
        val state = CognitiveStateClassifier.classify(
            switches = 25,
            shortSessions = 15,
            isLateNight = false,
            userIntent = "STUDY"
        )
        assertEquals(CognitiveState.STRESSED, state)
    }

    @Test
    fun testOverloadedState() {
        // Moderate switches and sessions
        val state = CognitiveStateClassifier.classify(
            switches = 15,
            shortSessions = 8,
            isLateNight = false,
            userIntent = "WORK"
        )
        assertEquals(CognitiveState.OVERLOADED, state)
    }

    @Test
    fun testDistractedLateNight() {
        // Late night behavior
        val state = CognitiveStateClassifier.classify(
            switches = 8,
            shortSessions = 4,
            isLateNight = true,
            userIntent = "SOCIAL"
        )
        assertEquals(CognitiveState.DISTRACTED, state)
    }
}
