package com.focusguardian.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusguardian.ui.dashboard.DashboardViewModel
import com.focusguardian.ui.dashboard.FocusState
import com.focusguardian.ui.theme.*
import kotlinx.coroutines.launch

class DashboardScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusGuardianTheme {
                DashboardContent(
                    onNavigateSettings = { startActivity(Intent(this, SettingsScreen::class.java)) },
                    onNavigateApps = { tabIndex -> 
                        val intent = Intent(this, com.focusguardian.ui.monitoring.MonitoringScreen::class.java).apply {
                            putExtra("start_tab", tabIndex)
                        }
                        startActivity(intent)
                    },
                    onNavigateSummary = { startActivity(Intent(this, com.focusguardian.ui.analytics.AnalyticsScreen::class.java)) },
                    onNavigateSchedules = { /* Pending */ },
                    viewModel = viewModel()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    onNavigateSettings: () -> Unit,
    onNavigateApps: (Int) -> Unit,
    onNavigateSummary: () -> Unit,
    onNavigateSchedules: () -> Unit,
    viewModel: DashboardViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val modeColor = try {
        Color(android.graphics.Color.parseColor(state.activeModeColor))
    } catch (e: Exception) {
        DeepBlue
    }
    
    val gradientColors = listOf(modeColor, DeepBlue)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DeepBlue,
                drawerContentColor = Color.White
            ) {
                Spacer(Modifier.height(24.dp))
                Text("Focus Guardian", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)
                Divider(color = Color.White.copy(alpha = 0.1f))
                NavigationDrawerItem(
                    label = { Text("Dashboard") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Dashboard, "Dashboard") }
                )
                NavigationDrawerItem(
                    label = { Text("Monitoring") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateApps(0) 
                    },
                    icon = { Icon(Icons.Default.Visibility, "Monitoring") }
                )
                NavigationDrawerItem(
                    label = { Text("Analytics") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateSummary() 
                    },
                    icon = { Icon(Icons.Default.Analytics, "Analytics") }
                )
                 NavigationDrawerItem(
                    label = { Text("Modes") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateSchedules() 
                    },
                    icon = { Icon(Icons.Default.Tune, "Modes") }
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { 
                        scope.launch { drawerState.close() }
                        onNavigateSettings()
                    },
                    icon = { Icon(Icons.Default.Settings, "Settings") }
                )
                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = false,
                    onClick = { 
                         // About Action or Dialog
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Info, "About") }
                )
                 NavigationDrawerItem(
                    label = { Text("Send Feedback") },
                    selected = false,
                    onClick = { 
                        // Feedback Intent
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Feedback, "Feedback") }
                )
            }
        }
    ) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(gradientColors))) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                NewDashboardHeader(
                    modeName = state.activeModeName,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )

                // Score Section
                NewStatusSection(
                    score = state.score,
                    modeName = state.activeModeName
                )

                // AI Insight Section (Below Focus Score per 3)
                AiInsightCard(
                    insight = state.customAiInsight ?: state.insightMessage,
                    isLoading = state.isAiLoading
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Monitoring Toggles
                MonitoringCard(
                    isAppEnabled = state.isAppMonitoringEnabled,
                    isWebEnabled = state.isWebsiteMonitoringEnabled,
                    isShortsEnabled = state.isShortsMonitoringEnabled,
                    onToggleApp = { viewModel.toggleAppMonitoring(it) },
                    onToggleWeb = { viewModel.toggleWebMonitoring(it) },
                    onToggleShorts = { viewModel.toggleShortsMonitoring(it) },
                    onNavigate = onNavigateApps
                )
                
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun NewDashboardHeader(
    modeName: String,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text("FOCUS GUARDIAN", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun NewStatusSection(score: Int, modeName: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
             // Glow
            Box(modifier = Modifier.size(180.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))
            
            Text(
                "$score",
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            "FOCUS SCORE",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryDark,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(16.dp))
        Surface(
            color = Color.White.copy(alpha = 0.1f),
            shape = RoundedCornerShape(50),
            modifier = Modifier.wrapContentSize()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(SuccessGreen))
                Spacer(Modifier.width(8.dp))
                // Matches Core 2: "Current Mode: (it was setted inside the mode menu...)"
                Text("Current Mode: $modeName", style = MaterialTheme.typography.bodyMedium, color = Color.White)
            }
        }
    }
}

@Composable
fun MonitoringCard(
    isAppEnabled: Boolean,
    isWebEnabled: Boolean,
    isShortsEnabled: Boolean,
    onToggleApp: (Boolean) -> Unit,
    onToggleWeb: (Boolean) -> Unit,
    onToggleShorts: (Boolean) -> Unit,
    onNavigate: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("ACTIVE MONITORING", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
            Spacer(Modifier.height(12.dp))
            MonitoringToggleRow("App Blocking", isAppEnabled, onToggleApp, onClick = { onNavigate(0) })
            MonitoringToggleRow("Website Blocking", isWebEnabled, onToggleWeb, onClick = { onNavigate(1) })
            MonitoringToggleRow("Shorts/Reels Filter", isShortsEnabled, onToggleShorts, onClick = { onNavigate(2) })
        }
    }
}

@Composable
fun MonitoringToggleRow(
    label: String, 
    isEnabled: Boolean, 
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.White)
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SeedPurple,
                uncheckedThumbColor = TextSecondaryDark,
                uncheckedTrackColor = Color.Transparent
            )
        )
    }
}

// Re-using existing components for consistency
@Composable
fun AiInsightCard(
    insight: String,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FOCUS AI INSIGHT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF6366F1),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (isLoading) {
                Text(
                    text = "Analyzing your daily patterns...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

