package com.focusguardian.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import androidx.core.app.NotificationCompat
// import com.focusguardian.logic.AlertModeHandler
import com.focusguardian.util.UserPrefs

/**
 * Foreground service that continuously monitors
 * the currently active foreground application.
 */
class AppUsageService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())

    private var currentApp: String? = null

    private val pollRunnable = object : Runnable {
        override fun run() {
            detectForegroundApp()
            handler.postDelayed(this, 2000) // poll every 2 seconds
        }
    }

    override fun onCreate() {
        super.onCreate()
        usageStatsManager =
            getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        startForegroundServiceInternal()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        currentApp?.let { AlertModeHandler.onAppExit(it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /* ==========================================================
       🔍 FOREGROUND APP DETECTION
       ========================================================== */

    private fun detectForegroundApp() {

        if (!UserPrefs.isAppMonitoringEnabled(this)) return
        if (UserPrefs.isEmergencyPauseActive(this)) return

        val endTime = System.currentTimeMillis()
        val startTime = endTime - 5000

        val events: UsageEvents =
            usageStatsManager.queryEvents(startTime, endTime)

        var event = UsageEvents.Event()
        var latestApp: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                latestApp = event.packageName
            }
        }

        // App changed
        if (latestApp != null && latestApp != currentApp) {
            currentApp?.let { AlertModeHandler.onAppExit(it) }
            currentApp = latestApp
        }

        // App still in foreground
        currentApp?.let {
            AlertModeHandler.onAppInForeground(this, it)
        }
    }

    /* ==========================================================
       🔔 FOREGROUND SERVICE NOTIFICATION
       ========================================================== */

    private fun startForegroundServiceInternal() {
        val channelId = "focus_guardian_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Focus Guardian Monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Guardian Active")
            .setContentText("Monitoring app usage to improve focus")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }
}
