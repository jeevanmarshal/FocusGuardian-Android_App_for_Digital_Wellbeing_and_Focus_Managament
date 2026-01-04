package com.focusguardian.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusguardian.util.PermissionChecker

class PermissionHealthScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF6C63FF),
                    secondary = Color(0xFF03DAC5),
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    onPrimary = Color.White,
                    onSurface = Color.White
                )
            ) {
                PermissionHealthContent(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionHealthContent(onBack: () -> Unit) {
    val context = LocalContext.current
    
    // Status states
    var hasUsageAccess by remember { mutableStateOf(PermissionChecker.hasUsageAccess(context)) }
    var hasNotificationPermission by remember { mutableStateOf(PermissionChecker.hasNotificationPermission(context)) }
    var isIgnoringBatteryOptimizations by remember { mutableStateOf(PermissionChecker.isIgnoringBatteryOptimizations(context)) }

    // Refresh status when returning to screen
    // Since this is a simple app, we can just rely on the user manually checking or re-composition on focus.
    // In a real app, an observer or onResume check would be better.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Permission Health") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "For Focus Guardian to work effectively, these permissions are essential.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            PermissionStatusCard(
                title = "Usage Access",
                description = "Required to detect which app is currently in use.",
                icon = Icons.Default.Settings,
                isGranted = hasUsageAccess,
                onFixClick = {
                    PermissionChecker.openUsageAccessSettings(context)
                }
            )

            PermissionStatusCard(
                title = "Notifications",
                description = "Allows the app to show alerts when you spend too much time on distracting apps.",
                icon = Icons.Default.Notifications,
                isGranted = hasNotificationPermission,
                onFixClick = {
                    PermissionChecker.openNotificationSettings(context)
                }
            )

            PermissionStatusCard(
                title = "Battery Optimization",
                description = "Ensures the service isn't killed in the background by system battery savers.",
                icon = Icons.Default.BatteryAlert,
                isGranted = isIgnoringBatteryOptimizations,
                onFixClick = {
                    PermissionChecker.openBatteryOptimizationSettings(context)
                }
            )
        }
    }
}

@Composable
fun PermissionStatusCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onFixClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isGranted) "Granted" else "Missing",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isGranted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            
            if (!isGranted) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onFixClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
