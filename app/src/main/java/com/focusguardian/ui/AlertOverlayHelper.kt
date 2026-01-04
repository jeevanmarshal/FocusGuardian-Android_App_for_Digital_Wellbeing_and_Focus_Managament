package com.focusguardian.ui

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.focusguardian.logic.AlertStage
import com.focusguardian.util.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object AlertOverlayHelper {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    
    // Lifecycle components for ComposeView
    private class OverlayLifecycleOwner : androidx.lifecycle.LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)
        private val _viewModelStore = ViewModelStore()

        override val lifecycle: Lifecycle = lifecycleRegistry
        override val savedStateRegistry: SavedStateRegistry = savedStateRegistryController.savedStateRegistry
        override val viewModelStore: ViewModelStore
            get() = _viewModelStore

        fun onCreate() {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }

        fun onDestroy() {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            viewModelStore.clear()
        }
    }
    
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    fun show(context: Context, stage: AlertStage, pkg: String, message: String, appName: String) {
        if (!Settings.canDrawOverlays(context)) {
            // Should not happen if permission is checked, but safety check.
            return
        }
        
        // Prevent looping/duplicate strict/blocked overlays
        if ((stage == AlertStage.STRICT || stage == AlertStage.BLOCKED) && overlayView != null) {
            // Already showing an overlay. Don't refresh constantly.
            return
        }

        // If an overlay is already showing, remove it first or update it.
        dismiss()

        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        
        lifecycleOwner = OverlayLifecycleOwner()
        lifecycleOwner?.onCreate()

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            // Flags: 
            // - FLAG_NOT_TOUCH_MODAL: Allow outside touches to go to other apps (if not full screen)
            // - FLAG_WATCH_OUTSIDE_TOUCH: To capture outside touches if needed
            // - FLAG_LAYOUT_IN_SCREEN: Use full screen including notification bar area
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or 
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, // Keep screen on for alert
            PixelFormat.TRANSLUCENT
        )
        
        // Ensure input is possible
        // If STRICT mode, we might want to block everything (FLAG_NOT_FOCUSABLE removed?)
        // The default behavior of creating a view allows touch events.
        // To allow typing or complex interaction, we might need more flags, 
        // but for buttons, default is fine.
        
        // Strict mode block logic
        if (stage == AlertStage.STRICT) {
            // In strict mode, we might want to consume back button? 
            // WindowManager views don't easily capture Back button unless focused.
        }

        params.gravity = Gravity.CENTER

        val pauseDuration = UserPrefs.getPauseDurationMinutes(context, pkg)
        val pendingTask = UserPrefs.getPendingTask(context, pkg)

        val composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setContent {
                MaterialTheme(
                    colorScheme = if (UserPrefs.getThemeMode(context) == 1) 
                        lightColorScheme() else darkColorScheme() 
                ) {
                    AlertOverlayContent(
                        stage = stage,
                        message = message,
                        pkg = pkg,
                        appName = appName,
                        pauseDuration = pauseDuration,
                        pendingTask = pendingTask,
                        onDismiss = { dismiss() },
                        onStrictAction = { strictAction(context, pkg) }
                    )
                }
            }
        }

        try {
            windowManager?.addView(composeView, params)
            overlayView = composeView
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback?
        }
    }

    fun dismiss() {
        try {
            if (overlayView != null && windowManager != null) {
                lifecycleOwner?.onDestroy()
                windowManager?.removeView(overlayView)
                overlayView = null
                lifecycleOwner = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun strictAction(context: Context, pkg: String) {
        // Enforce block
        // 1. Check if Strict Enforcement (Blocking) is enabled
        // The Prompt states: "If toggle is ON -> strict alert exits app + blocks app for X minutes"
        // "If toggle is OFF -> strict alert exits app only"
        if (UserPrefs.isStrictEnabled(context, pkg)) {
            val pauseDuration = UserPrefs.getPauseDurationMinutes(context, pkg)
            // Default pause duration if 0? No, UserPrefs defaults to 5.
            if (pauseDuration > 0) {
                val pauseUntil = System.currentTimeMillis() + (pauseDuration * 60 * 1000L)
                UserPrefs.setAppPausedUntil(context, pkg, pauseUntil)
            }
        }

        // 2. Go Home (Always enforced for Strict Alert)
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(homeIntent)
        
        // 3. Dismiss overlay
        dismiss()
    }
}

@Composable
fun AlertOverlayContent(
    stage: AlertStage,
    message: String,
    pkg: String,
    appName: String,
    pauseDuration: Int,
    pendingTask: String,
    onDismiss: () -> Unit,
    onStrictAction: () -> Unit
) {
    val context = LocalContext.current
    // Sound & Vibration for Strict Mode (Max 3 seconds)
    LaunchedEffect(stage) {
        if (stage == AlertStage.STRICT) {
            withContext(Dispatchers.IO) {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val toneGen = try {
                    ToneGenerator(AudioManager.STREAM_ALARM, 100)
                } catch (e: Exception) {
                    null
                }

                try {
                    // Play for EXACTLY 3 seconds once
                    
                    // Vibration (One shot, 3 seconds)
                    try {
                        if (Build.VERSION.SDK_INT >= 26) {
                            vibrator?.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            vibrator?.vibrate(3000)
                        }
                    } catch (e: Exception) {}

                    // Sound (One shot, 3 seconds)
                    try {
                         // TONE_CDMA_EMERGENCY_RINGBACK or TONE_DTMF_S depending on what's "danger". 
                         // Emergency ringback is usually quite alerting.
                        toneGen?.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 3000) 
                    } catch (e: Exception) {}

                    delay(3000) // Wait for sound to finish
                    
                    // Cleanup
                    try {
                        toneGen?.stopTone()
                    } catch (e: Exception) {}
                    
                } finally {
                    try {
                        toneGen?.release()
                    } catch (e: Exception) {}
                }
            }
        }
    }
    
    // BLOCKED UI
    if (stage == AlertStage.BLOCKED) {
        val currentPendingTask = UserPrefs.getPendingTask(context, pkg)
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.98f))
                .clickable(enabled = false) {}, 
            contentAlignment = Alignment.Center
        ) {
             Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier.size(80.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.Text(
                    text = "App access is blocked.",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                androidx.compose.material3.Text(
                    text = "Get back to your pending task: ${if (currentPendingTask.isNotEmpty()) currentPendingTask else "Focus Breakdown"}.",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
                 Spacer(modifier = Modifier.height(48.dp))
                 
                 Row(
                     modifier = Modifier.fillMaxWidth(),
                     horizontalArrangement = Arrangement.spacedBy(16.dp)
                 ) {
                     // Exit Button
                     androidx.compose.material3.Button(
                         onClick = {
                             val home = Intent(Intent.ACTION_MAIN)
                             home.addCategory(Intent.CATEGORY_HOME)
                             home.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                             context.startActivity(home)
                             onDismiss()
                         },
                         colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                         modifier = Modifier.weight(1f)
                     ) {
                         androidx.compose.material3.Text("Exit")
                     }

                     // Unlock Button
                     androidx.compose.material3.Button(
                         onClick = {
                             // Redirect to App Config / Manual Override
                             try {
                                 val intent = Intent(context, com.focusguardian.ui.AppConfigScreen::class.java).apply {
                                     putExtra("pkg", pkg)
                                     putExtra("appName", appName)
                                     flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                 }
                                 context.startActivity(intent)
                                 onDismiss()
                             } catch (e: Exception) {
                                 e.printStackTrace()
                             }
                         },
                         colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                         modifier = Modifier.weight(1f)
                     ) {
                         androidx.compose.material3.Text("Unlock")
                     }
                 }
            }
        }
        return
    }

    // Standard Alerts (Gentle, Reminder, Strict)
    val (title, icon, color) = when (stage) {
        AlertStage.GENTLE -> Triple("Focus Hint", Icons.Default.NotificationsActive, Color(0xFF6C63FF))
        AlertStage.REMINDER -> Triple("Wellness Nudge", Icons.Default.Warning, Color(0xFFFFB74D))
        AlertStage.STRICT -> Triple("Time's Up!", Icons.Default.Block, Color(0xFFF44336))
        else -> Triple("Alert", Icons.Default.Warning, Color.Gray)
    }

    val isFullScreen = stage == AlertStage.STRICT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (isFullScreen) 0.95f else 0.0f)) 
            .clickable(enabled = isFullScreen) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1E1E)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(56.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                androidx.compose.material3.Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                androidx.compose.material3.Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (stage == AlertStage.STRICT) {
                    androidx.compose.material3.Button(
                        onClick = onStrictAction, // Action to Block + Exit
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        androidx.compose.material3.Text("Close App")
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            androidx.compose.material3.Text("Dismiss")
                        }
                        androidx.compose.material3.Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            androidx.compose.material3.Text("OK")
                        }
                    }
                }
            }
        }
    }
}
