package com.focusguardian.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.focusguardian.ServiceLocator
import com.focusguardian.domain.logic.EnforcementExecutor
import com.focusguardian.domain.logic.OverlayController
import com.focusguardian.ui.overlay.AccessBlockedOverlay
import com.focusguardian.ui.overlay.GentleAlertOverlay
import com.focusguardian.ui.overlay.ReminderAlertOverlay
import com.focusguardian.ui.overlay.StrictBlockingOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayService : Service(), SavedStateRegistryOwner, LifecycleOwner {

    // Dependencies
    private val overlayController: OverlayController
        get() = ServiceLocator.overlayController
    
    private val enforcementExecutor: EnforcementExecutor
        get() = ServiceLocator.enforcementExecutor

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private var params: WindowManager.LayoutParams? = null

    // Manual Scope & Lifecycle
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        // Initialize Overlay View
        // Use this@OverlayService explicitly as Context
        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
        }

        // Window Params
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        observeOverlayState()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        serviceScope.cancel()
        removeOverlay()
    }

    private fun observeOverlayState() {
        serviceScope.launch {
            overlayController.overlayState.collectLatest { state ->
                when (state) {
                    is OverlayController.OverlayState.None -> removeOverlay()
                    is OverlayController.OverlayState.Gentle -> showGentle(state)
                    is OverlayController.OverlayState.Reminder -> showReminder(state)
                    is OverlayController.OverlayState.Strict -> showStrict(state)
                    is OverlayController.OverlayState.AccessBlocked -> showBlocked(state)
                }
            }
        }
    }

    private fun showGentle(state: OverlayController.OverlayState.Gentle) {
        setTextContent {
            GentleAlertOverlay(
                appName = state.targetName,
                customMessage = state.message,
                onOkClick = { overlayController.hideOverlay() },
                onExitClick = { 
                    overlayController.hideOverlay()
                    enforcementExecutor.executeGoHome() 
                }
            )
        }
        addOverlay()
    }

    private fun showReminder(state: OverlayController.OverlayState.Reminder) {
        setTextContent {
            ReminderAlertOverlay(
                appName = state.targetName,
                customMessage = state.message,
                onOkClick = { overlayController.hideOverlay() },
                onExitClick = {
                    overlayController.hideOverlay()
                    enforcementExecutor.executeGoHome()
                }
            )
        }
        addOverlay()
    }

    private fun showStrict(state: OverlayController.OverlayState.Strict) {
        setTextContent {
            StrictBlockingOverlay(
                appName = state.targetName,
                customMessage = state.message,
                timeLeftSeconds = state.durationSeconds,
                onCloseClick = {
                    overlayController.hideOverlay()
                    enforcementExecutor.executeGoHome()
                }
            )
        }
        addOverlay()
    }

    private fun showBlocked(state: OverlayController.OverlayState.AccessBlocked) {
        setTextContent {
            AccessBlockedOverlay(
                appName = state.targetName,
                pendingTask = state.pendingTask,
                onUnlockClick = {
                    // Start Activity using Service Context 
                    val intent = Intent(this@OverlayService, com.focusguardian.MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("route", "config_screen")
                    intent.putExtra("target", state.targetName)
                    startActivity(intent)
                    overlayController.hideOverlay()
                },
                onExitClick = {
                    overlayController.hideOverlay()
                    enforcementExecutor.executeGoHome()
                }
            )
        }
        addOverlay()
    }

    private fun setTextContent(content: @androidx.compose.runtime.Composable () -> Unit) {
        overlayView?.setContent(content)
    }

    private fun addOverlay() {
        if (overlayView?.parent == null) {
            windowManager.addView(overlayView, params)
        } else {
            windowManager.updateViewLayout(overlayView, params)
        }
    }

    private fun removeOverlay() {
        if (overlayView?.parent != null) {
            windowManager.removeView(overlayView)
        }
    }
}
