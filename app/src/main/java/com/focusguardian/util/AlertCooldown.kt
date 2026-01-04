package com.focusguardian.util

object AlertCooldown {
    private val lastAlertMap = mutableMapOf<String, Long>()
    private const val COOLDOWN_MS = 60_000 // 1 minute

    fun canTrigger(pkg: String): Boolean {
        val now = System.currentTimeMillis()
        val lastTime = lastAlertMap[pkg] ?: 0L
        if (now - lastTime > COOLDOWN_MS) {
            lastAlertMap[pkg] = now
            return true
        }
        return false
    }

    fun reset(pkg: String) {
        lastAlertMap.remove(pkg)
    }
}
