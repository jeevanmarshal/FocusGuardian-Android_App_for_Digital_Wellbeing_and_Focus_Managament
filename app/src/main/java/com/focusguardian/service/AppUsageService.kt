package com.focusguardian.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import androidx.core.app.NotificationCompat
import com.focusguardian.logic.AlertModeHandler
import com.focusguardian.logic.CognitiveFeatureExtractor
import com.focusguardian.util.UserPrefs
import com.focusguardian.data.UsageDatabaseHelper
import com.focusguardian.logic.AlertDispatcher
import com.focusguardian.logic.AlertStage

/**
 * Foreground service that continuously monitors
 * the currently active foreground application.
 */
class AppUsageService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dbHelper: UsageDatabaseHelper

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
        
        dbHelper = UsageDatabaseHelper(this)

        
        AlertModeHandler.resetAll()
        VoiceAlertService.init(this)

        startForegroundServiceInternal()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        currentApp?.let { AlertModeHandler.onAppExit(this, it) }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /* ==========================================================
       🔍 FOREGROUND APP DETECTION
       ========================================================== */

    private fun detectForegroundApp() {

        if (!UserPrefs.isAppMonitoringEnabled(this)) return
        if (UserPrefs.isEmergencyPauseActive(this)) return

        // 0. CHECK FOCUS MODE
        if (UserPrefs.isFocusModeActive(this)) {
             // We can't know the app yet, let's query event first.
        }


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

        // Exclude Focus Guardian itself
        if (latestApp == packageName) {
            if (currentApp != null && currentApp != packageName) {
                AlertModeHandler.onAppExit(this, currentApp!!)
            }
            currentApp = null
            return
        }

        // App changed
        if (latestApp != null && latestApp != currentApp) {
            currentApp?.let { AlertModeHandler.onAppExit(this, it) }
            currentApp = latestApp
            UserPrefs.incrementSwitchCount(this)
            UserPrefs.incrementAppUsageCount(this, latestApp)
            UserPrefs.setLastUsedTimestamp(this, latestApp)
            CognitiveFeatureExtractor.recordAppSwitch(latestApp)
            dbHelper.incrementLaunch(latestApp)
        }


        // App still in foreground
        currentApp?.let {
            
            // Check Focus Mode / Bedtime Blocking
            val isFocus = UserPrefs.isFocusModeActive(this)
            val isBedtime = com.focusguardian.logic.BedtimeManager.isBedtimeActive(this)

            if ((isFocus || isBedtime) && UserPrefs.isAppInFocusMode(this, it)) {
                AlertDispatcher.dispatch(this, AlertStage.BLOCKED, it)
                return // Do not track time
            }

            AlertModeHandler.onAppInForeground(this, it)
            dbHelper.addUsage(it, 2000L)
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                1, 
                notification, 
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(1, notification)
        }
    }
}
