package com.focusguardian.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.focusguardian.ServiceLocator
import com.focusguardian.domain.logic.RuleManager
import kotlinx.coroutines.*

class AppUsageService : Service() {

    private lateinit var usageStatsManager: UsageStatsManager
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val sessionUsageMap = mutableMapOf<String, Long>()
    private val handler = Handler(Looper.getMainLooper())

    private val pollRunnable = object : Runnable {
        override fun run() {
            monitorUsage()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
        
        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        
        startForegroundServiceInternal()
        handler.post(pollRunnable)
    }

    override fun onDestroy() {
        handler.removeCallbacks(pollRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun monitorUsage() {
        try {
            // 1. Detect Foreground App
            val endTime = System.currentTimeMillis()
            val startTime = endTime - 2000
            val events = usageStatsManager.queryEvents(startTime, endTime)
            var currentApp: String? = null
            val event = UsageEvents.Event()
            
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    currentApp = event.packageName
                }
            }
            
            // If we can't detect via stats, maybe fallback to last known or ignore.
            // But we need to check ContextManager for Shorts even if stats says YouTube.
            // If currentApp is null, we might still be in same app. 
            // Better to rely on what stats/accessibility thinks.
            // For this impl, assume if currentApp is null, we pause monitoring or rely on stored "last foreground".
            if (currentApp == null) return

            // 2. Persistent Usage Tracking for APP
            scope.launch(Dispatchers.IO) {
                val dataRepository = ServiceLocator.dataRepository
                val usageDao = dataRepository.getUsageDao()
                val today = getMidnightTimestamp()
                
                // Track Generic App Usage
                var usage = usageDao.getAppUsage(currentApp!!, today)
                if (usage == null) {
                    usage = com.focusguardian.data.local.entity.AppUsageEntity(
                        packageName = currentApp!!,
                        date = today,
                        usageDurationMillis = 0,
                        openCount = 1
                    )
                }
                
                val updatedUsage = usage.copy(
                     usageDurationMillis = usage.usageDurationMillis + 1000,
                     openCount = usage.openCount // Open count logic: ideally handled on MOVE_TO_FOREGROUND only
                )
                usageDao.insertOrUpdateAppUsage(updatedUsage)
                
                // 3. Shorts/Reels Logic
                var isShortsActive = false
                var isReelsActive = false
                
                if (currentApp == "com.google.android.youtube") {
                    if (com.focusguardian.domain.logic.CurrentContextManager.isShortsVisible()) {
                        isShortsActive = true
                        trackSpecialUsage(usageDao, "youtube_shorts", today)
                    }
                } else if (currentApp == "com.instagram.android") {
                     if (com.focusguardian.domain.logic.CurrentContextManager.isReelsVisible()) {
                        isReelsActive = true
                        trackSpecialUsage(usageDao, "insta_reels", today)
                    }
                }

                // 4. Evaluate Rules (Switch back to main for UI)
                val totalSeconds = updatedUsage.usageDurationMillis / 1000
                withContext(Dispatchers.Main) {
                    val ruleManager = ServiceLocator.ruleManager
                    val overlayController = ServiceLocator.overlayController
                    val enforcementExecutor = ServiceLocator.enforcementExecutor
                    
                    // Evaluate App Rule
                    var decision = ruleManager.evaluate(currentApp!!, RuleManager.TargetType.APP, totalSeconds)
                    
                    // Override decision if Shorts/Reels active
                    if (isShortsActive) {
                        val shortsUsage = withContext(Dispatchers.IO) { usageDao.getAppUsage("youtube_shorts", today)?.usageDurationMillis ?: 0L }
                        decision = ruleManager.evaluate("Shorts", RuleManager.TargetType.SHORTS, shortsUsage / 1000)
                    } else if (isReelsActive) {
                        val reelsUsage = withContext(Dispatchers.IO) { usageDao.getAppUsage("insta_reels", today)?.usageDurationMillis ?: 0L }
                        decision = ruleManager.evaluate("Reels", RuleManager.TargetType.REELS, reelsUsage / 1000)
                    }
                    
                    when (decision) {
                        RuleManager.Decision.ALLOW -> {
                            overlayController.hideOverlay()
                        }
                        RuleManager.Decision.GENTLE_ALERT -> {
                            overlayController.showGentleAlert(if(isShortsActive) "Shorts" else if(isReelsActive) "Reels" else currentApp!!, "Gentle Reminder: Time is passing!")
                        }
                        RuleManager.Decision.REMINDER_ALERT -> {
                            overlayController.showReminderAlert(if(isShortsActive) "Shorts" else if(isReelsActive) "Reels" else currentApp!!, "Review your focus goals.")
                        }
                        RuleManager.Decision.STRICT_BLOCK -> {
                            overlayController.showStrictAlert(if(isShortsActive) "Shorts" else if(isReelsActive) "Reels" else currentApp!!, "Time limit reached.", 20)
                            enforcementExecutor.executeGoHome()
                        }
                        RuleManager.Decision.BLOCK_FEED -> {
                            // Specifically for Shorts/Reels feed blocking
                            // We can try Back action via Accessibility Service if possible, or just Go Home
                            enforcementExecutor.executeGoHome()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private suspend fun trackSpecialUsage(usageDao: com.focusguardian.data.local.dao.UsageDao, pkg: String, date: Long) {
         var usage = usageDao.getAppUsage(pkg, date)
         if (usage == null) {
            usage = com.focusguardian.data.local.entity.AppUsageEntity(
                 packageName = pkg,
                 date = date,
                 usageDurationMillis = 0,
                 openCount = 1
            )
         }
         val updated = usage.copy(usageDurationMillis = usage.usageDurationMillis + 1000)
         usageDao.insertOrUpdateAppUsage(updated)
    }

    private fun getMidnightTimestamp(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    private fun startForegroundServiceInternal() {
        val channelId = "focus_guardian_mon_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Focus Guardian Active",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Focus Guardian Active")
            .setContentText("Monitoring usage...")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setOngoing(true)
            .build()
            
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }
    }
}
