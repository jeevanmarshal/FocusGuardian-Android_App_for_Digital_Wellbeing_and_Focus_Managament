package com.focusguardian.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguardian.logic.AlertStage
import com.focusguardian.util.UserPrefs

class FocusAlertActivity : ComponentActivity() {

    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val stageName = intent.getStringExtra("stage") ?: "GENTLE"
        val message = intent.getStringExtra("message") ?: "Time to focus!"
        val pkg = intent.getStringExtra("pkg") ?: "this app"
        
        val stage = try { AlertStage.valueOf(stageName) } catch (e: Exception) { AlertStage.GENTLE }
        
        // Strict Mode Logic
        if (stage == AlertStage.STRICT) {
            startStrictCountdown(pkg)
        }

        setContent {
            val themeMode = UserPrefs.getThemeMode(this)
            val isDark = when (themeMode) {
                1 -> false // Light
                2 -> true  // Dark
                else -> androidx.compose.foundation.isSystemInDarkTheme() // System
            }

            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme(
                    primary = Color(0xFF6C63FF),
                    surface = Color(0xFF1E1E1E),
                    onSurface = Color.White
                ) else lightColorScheme(
                    primary = Color(0xFF6200EE),
                    surface = Color(0xFFFFFFFF),
                    onSurface = Color.Black
                )
            ) {
                AlertOverlay(
                    stage = stage,
                    message = message,
                    pkg = pkg,
                    onDismiss = { if (stage != AlertStage.STRICT) finish() },
                    onAction = {
                        if (stage == AlertStage.REMINDER) {
                            val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        finish()
                    }
                )
            }
        }
    }

    private fun startStrictCountdown(pkg: String) {
        countdownTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Timer is handled in UI state if needed, but here we just manage the exit
            }

            override fun onFinish() {
                enforceStrictBlock(pkg)
            }
        }.start()
    }

    private fun enforceStrictBlock(pkg: String) {
        // 1. Set pause/block
        val pauseDuration = UserPrefs.getPauseDurationMinutes(this, pkg)
        val pauseUntil = System.currentTimeMillis() + (pauseDuration * 60 * 1000L)
        UserPrefs.setAppPausedUntil(this, pkg, pauseUntil)

        // 2. Go Home
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
        
        // 3. Finish this alert
        finish()
    }

    override fun onDestroy() {
        countdownTimer?.cancel()
        super.onDestroy()
    }
}

@Composable
fun AlertOverlay(
    stage: AlertStage,
    message: String,
    pkg: String,
    onDismiss: () -> Unit,
    onAction: () -> Unit
) {
    var countdownSeconds by remember { mutableStateOf(5) }
    
    // UI-side countdown for visual feedback in STRICT mode
    if (stage == AlertStage.STRICT) {
        LaunchedEffect(Unit) {
            while (countdownSeconds > 0) {
                kotlinx.coroutines.delay(1000)
                countdownSeconds--
            }
        }
    }

    val (title, icon, color) = when (stage) {
        AlertStage.GENTLE -> Triple("Focus Hint", Icons.Default.NotificationsActive, Color(0xFF6C63FF))
        AlertStage.REMINDER -> Triple("Wellness Nudge", Icons.Default.Warning, Color(0xFFFFB74D))
        AlertStage.STRICT -> Triple("Focus Shield", Icons.Default.Block, Color(0xFFF44336))
        else -> Triple("Alert", Icons.Default.Warning, Color.Gray)
    }

    val isFullScreen = stage == AlertStage.STRICT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = if (isFullScreen) 0.95f else 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(72.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                if (stage == AlertStage.STRICT) {
                    Text(
                        text = "Closing in $countdownSeconds",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "App will be paused.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ignore")
                        }
                        
                        Button(
                            onClick = onAction,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            Text(if (stage == AlertStage.REMINDER) "Open Task" else "OK")
                        }
                    }
                }
            }
        }
    }
}
