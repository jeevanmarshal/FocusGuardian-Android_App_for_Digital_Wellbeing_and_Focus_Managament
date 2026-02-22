package com.focusguardian.domain.logic

import java.util.concurrent.atomic.AtomicLong

object CurrentContextManager {
    private val lastShortsDetection = AtomicLong(0)
    private val lastReelsDetection = AtomicLong(0)
    private const val TIMEOUT_MS = 2000L // If not seen for 2s, assume not watching

    fun reportShortsVisible() {
        lastShortsDetection.set(System.currentTimeMillis())
    }

    fun reportReelsVisible() {
        lastReelsDetection.set(System.currentTimeMillis())
    }

    fun isShortsVisible(): Boolean {
        return (System.currentTimeMillis() - lastShortsDetection.get()) < TIMEOUT_MS
    }

    fun isReelsVisible(): Boolean {
        return (System.currentTimeMillis() - lastReelsDetection.get()) < TIMEOUT_MS
    }
}
