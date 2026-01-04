package com.focusguardian.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguardian.util.FocusScoreUtil
import com.focusguardian.util.UserPrefs

class DashboardScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode = UserPrefs.getThemeMode(this)
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MaterialTheme(
                colorScheme = if (isDark) darkColorScheme(
                    primary = Color(0xFF6C63FF),
                    secondary = Color(0xFF03DAC5),
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    onPrimary = Color.White,
                    onSurface = Color.White
                ) else lightColorScheme(
                    primary = Color(0xFF6200EE),
                    secondary = Color(0xFF03DAC5),
                    background = Color(0xFFFFFFFF),
                    surface = Color(0xFFF5F5F5),
                    onPrimary = Color.White,
                    onSurface = Color.Black
                )
            ) {
                DashboardContent(
                    onNavigateSettings = {
                        startActivity(Intent(this, SettingsScreen::class.java))
                    },
                    onNavigateApps = {
                        startActivity(Intent(this, AppSelectionScreen::class.java))
                    },
                    onNavigateSummary = {
                        startActivity(Intent(this, WeeklySummaryScreen::class.java))
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-trigger recomposition by recreating activity for fresh data
        // In a real app, use a ViewModel/Flow to observe data changes.
        // This is a temporary simple refresh mechanism.
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    onNavigateSettings: () -> Unit,
    onNavigateApps: () -> Unit,
    onNavigateSummary: () -> Unit
) {
    val context = LocalContext.current
    var score by remember { mutableStateOf(0) }
    var focusedMinutes by remember { mutableStateOf(0) }
    var distractedMinutes by remember { mutableStateOf(0) }
    var cognitiveState by remember { mutableStateOf(com.focusguardian.logic.CognitiveState.CALM) }
    var aiInsight by remember { mutableStateOf("") }
    
    // Focus Mode State
    var isFocusMode by remember { mutableStateOf(UserPrefs.isFocusModeActive(context)) }

    // Fetch data when Composable enters composition and poll every 30s
    LaunchedEffect(Unit) {
        while (true) {
            score = com.focusguardian.util.FocusScoreUtil.getTodayScore(context)
            focusedMinutes = com.focusguardian.util.FocusScoreUtil.getFocusedTime(context)
            distractedMinutes = com.focusguardian.util.FocusScoreUtil.getDistractedTime(context)
            cognitiveState = com.focusguardian.logic.CognitiveAnalyzer.getCognitiveLoad(context)
            aiInsight = com.focusguardian.logic.CognitiveAnalyzer.getAIInsight(context)
            kotlinx.coroutines.delay(30000) // Refresh every 30 seconds
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Focus Guardian",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp), // Minimal vertical padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Distribute space evenly
        ) {
            


            // Mode Selection Button (Replaces Intent Selector)
            var currentGlobalIntent by remember { mutableStateOf(UserPrefs.getGlobalIntent(context)) }
            val isBedtime = UserPrefs.isBedtimeEnabled(context)
            val modeDisplay = if (isBedtime) "Bedtime" else currentGlobalIntent

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(context, ModeSelectionScreen::class.java))
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Current Mode", color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = modeDisplay,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Circular Score & Cognitive Load (Flexible Size)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.weight(1f).fillMaxWidth() // Let it take available space
            ) {
                // Background Glow
                Box(
                    modifier = Modifier
                        .size(180.dp) // Reduced initial size
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(160.dp), // Reduced from 200
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.size(160.dp), // Reduced from 200
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                    Text(
                        text = "FOCUS SCORE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // AI Insight Card (Samsung Style) - Compact
            Card(
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 140.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Insight",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Cognitive State Tag
                        Surface(
                            color = when(cognitiveState) {
                                com.focusguardian.logic.CognitiveState.CALM -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                com.focusguardian.logic.CognitiveState.DISTRACTED -> Color(0xFFFFC107).copy(alpha = 0.1f)
                                com.focusguardian.logic.CognitiveState.OVERLOADED -> Color(0xFFFF9800).copy(alpha = 0.1f)
                                com.focusguardian.logic.CognitiveState.STRESSED -> Color(0xFFF44336).copy(alpha = 0.1f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = cognitiveState.name,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = when(cognitiveState) {
                                    com.focusguardian.logic.CognitiveState.CALM -> Color(0xFF4CAF50)
                                    com.focusguardian.logic.CognitiveState.DISTRACTED -> Color(0xFFFFC107)
                                    com.focusguardian.logic.CognitiveState.OVERLOADED -> Color(0xFFFF9800)
                                    com.focusguardian.logic.CognitiveState.STRESSED -> Color(0xFFF44336)
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = aiInsight,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Usage Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Focused",
                    value = "${focusedMinutes}m",
                    color = MaterialTheme.colorScheme.primary
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Distracted",
                    value = "${distractedMinutes}m",
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompactMenuButton(
                    modifier = Modifier.weight(1f),
                    text = "Apps",
                    icon = Icons.Default.List,
                    onClick = onNavigateApps
                )
                CompactMenuButton(
                    modifier = Modifier.weight(1f),
                    text = "History",
                    icon = Icons.Default.History,
                    onClick = onNavigateSummary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CompactMenuButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
      