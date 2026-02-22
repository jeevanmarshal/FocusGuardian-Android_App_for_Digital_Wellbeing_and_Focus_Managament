package com.focusguardian.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.focusguardian.ServiceLocator
import com.focusguardian.domain.logic.RuleManager
import com.focusguardian.domain.logic.CurrentContextManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var currentMonitoredPackage: String? = null
    private var launcherPackageName: String? = null

    override fun onCreate() {
        super.onCreate()
        resolveLauncherPackage()
        ServiceLocator.initialize(applicationContext)
    }
    
    private fun resolveLauncherPackage() {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
            intent.addCategory(android.content.Intent.CATEGORY_HOME)
            val resolveInfo = packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            launcherPackageName = resolveInfo?.activityInfo?.packageName
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        // Window changes / content changes
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            val packageName = event.packageName?.toString() ?: return
            
            // Ignore self or launcher
            if (packageName == this.packageName || packageName == launcherPackageName || packageName == "com.android.systemui") return
            
            // 1. App Blocking (Check on window change)
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (currentMonitoredPackage != packageName) {
                    currentMonitoredPackage = packageName
                    // Reset/Start timer logic if needed
                }
                
                // Evaluate Rule
                val decision = ServiceLocator.ruleManager.evaluate(packageName, RuleManager.TargetType.APP, 0L) // Usage 0 for immediate check
                if (decision == RuleManager.Decision.STRICT_BLOCK) {
                     performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                     ServiceLocator.overlayController.showStrictAlert(packageName, "Strictly Blocked", 0)
                }
            }
            
            // 2. Shorts/Reels & Website Logic
            checkShortsAndReels(event, packageName)
        }
    }

    private fun checkShortsAndReels(event: AccessibilityEvent, packageName: String) {
        val rootNode = rootInActiveWindow ?: return
        
        // YouTube Shorts detection
        if (packageName == "com.google.android.youtube") {
            val isShorts = findNodeByText(rootNode, "Shorts") || findNodeByResId(rootNode, "com.google.android.youtube:id/shorts_player")
            if (isShorts) {
                 // Report visibility for time tracking
                 CurrentContextManager.reportShortsVisible()
                 
                 // Immediate Blocking Eval (for Strict Mode entry blocking)
                 val decision = ServiceLocator.ruleManager.evaluate("Shorts", RuleManager.TargetType.SHORTS, 0L)
                 if (decision == RuleManager.Decision.STRICT_BLOCK || decision == RuleManager.Decision.BLOCK_FEED) {
                      performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                      ServiceLocator.overlayController.showStrictAlert("YouTube Shorts", "Shorts are Restricted", 0)
                 }
            }
        }
        
        // Instagram Reels detection
        if (packageName == "com.instagram.android") {
            val isReels = findNodeByResId(rootNode, "com.instagram.android:id/reels_viewer_container")
            if (isReels) {
                 CurrentContextManager.reportReelsVisible()
                 
                 val decision = ServiceLocator.ruleManager.evaluate("Reels", RuleManager.TargetType.REELS, 0L)
                 if (decision == RuleManager.Decision.STRICT_BLOCK || decision == RuleManager.Decision.BLOCK_FEED) {
                      performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
                      ServiceLocator.overlayController.showStrictAlert("Instagram Reels", "Reels are Restricted", 0)
                 }
            }
        }
    }

    private fun findNodeByText(node: android.view.accessibility.AccessibilityNodeInfo, text: String): Boolean {
        val nodes = node.findAccessibilityNodeInfosByText(text)
        return nodes != null && nodes.isNotEmpty()
    }

    private fun findNodeByResId(node: android.view.accessibility.AccessibilityNodeInfo, resId: String): Boolean {
        val nodes = node.findAccessibilityNodeInfosByViewId(resId)
        return nodes != null && nodes.isNotEmpty()
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }
}
