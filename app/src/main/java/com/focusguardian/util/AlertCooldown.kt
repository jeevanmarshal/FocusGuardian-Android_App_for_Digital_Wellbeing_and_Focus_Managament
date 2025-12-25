package com.focusguardian.util

object AlertCooldown {
    private var lastAlertTime = 0L
    private const val COOLDOWN_MS = 60_000 // 1 minute

    fun canTrigger(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastAlertTime > COOLDOWN_MS) {
            lastAlertTime = now
            return true
        }
        return false
    }
}
