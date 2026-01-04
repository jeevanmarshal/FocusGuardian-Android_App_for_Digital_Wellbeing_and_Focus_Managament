package com.focusguardian.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusguardian.util.UserPrefs

data class ModeItem(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

class ModeSelectionScreen : ComponentActivity() {
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
                    surface = Color(0xFF1E1E1E)
                ) else lightColorScheme(
                    primary = Color(0xFF6200EE),
                    secondary = Color(0xFF03DAC5),
                    background = Color(0xFFF5F5F5),
                    surface = Color.White
                )
            ) {
                ModeSelectionContent(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionContent(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentIntent by remember { mutableStateOf(UserPrefs.getGlobalIntent(context)) }
    var isBedtime by remember { mutableStateOf(UserPrefs.isBedtimeEnabled(context)) }
    
    // Overlay State
    var selectedMode by remember { mutableStateOf<ModeItem?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val modes = listOf(
        ModeItem("Work", Icons.Default.Work, Color(0xFF2196F3), "Professional focus, strict blocks"),
        ModeItem("Study", Icons.Default.School, Color(0xFFFF9800), "Learning mode, gentle reminders"),
        ModeItem("Focus", Icons.Default.CenterFocusStrong, Color(0xFF4CAF50), "Deep work, minimize distractions"),
        ModeItem("Bedtime", Icons.Default.Bedtime, Color(0xFF673AB7), "Sleep protection, full blocking"),
        ModeItem("Custom", Icons.Default.Edit, Color(0xFF9C27B0), "Personalized settings")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Mode", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Choose a mode to optimize your environment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(modes) { mode ->
                    val isSelected = if (mode.name == "Bedtime") isBedtime else (currentIntent == mode.name && !isBedtime)
                    
                    ModeCard(
                        mode = mode,
                        isSelected = isSelected,
                        onClick = {
                            selectedMode = mode
                            showDialog = true
                        }
                    )
                }
            }
        }

        // Overlay Configuration
        if (showDialog && selectedMode != null) {
            val mode = selectedMode!!
            
            androidx.compose.ui.window.Dialog(onDismissRequest = { showDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = mode.icon, 
                            contentDescription = null, 
                            tint = mode.color,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = mode.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = mode.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Mode Specific Configuration
                        var tempStartH by remember { mutableIntStateOf(UserPrefs.getBedtimeStartHour(context)) }
                        var tempStartM by remember { mutableIntStateOf(UserPrefs.getBedtimeStartMinute(context)) }
                        var tempEndH by remember { mutableIntStateOf(UserPrefs.getBedtimeEndHour(context)) }
                        var tempEndM by remember { mutableIntStateOf(UserPrefs.getBedtimeEndMinute(context)) }

                        if (mode.name == "Bedtime") {
                            BedtimeConfig(
                                startH = tempStartH, onStartHChange = { tempStartH = it },
                                startM = tempStartM, onStartMChange = { tempStartM = it },
                                endH = tempEndH, onEndHChange = { tempEndH = it },
                                endM = tempEndM, onEndMChange = { tempEndM = it }
                            )
                        } 
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { showDialog = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = {
                                    if (mode.name == "Bedtime") {
                                        UserPrefs.setBedtimeStart(context, tempStartH, tempStartM)
                                        UserPrefs.setBedtimeEnd(context, tempEndH, tempEndM)
                                    }
                                    
                                    applyMode(context, mode)
                                    currentIntent = UserPrefs.getGlobalIntent(context)
                                    isBedtime = UserPrefs.isBedtimeEnabled(context)
                                    Toast.makeText(context, "${mode.name} Activated", Toast.LENGTH_SHORT).show()
                                    showDialog = false
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = mode.color)
                            ) {
                                Text("Activate")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BedtimeConfig(
    startH: Int, onStartHChange: (Int) -> Unit,
    startM: Int, onStartMChange: (Int) -> Unit,
    endH: Int, onEndHChange: (Int) -> Unit,
    endM: Int, onEndMChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Schedule", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Start Time Row
        Text("Start Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsTimeWheel("Hour", startH, 0..23, onStartHChange)
            Spacer(modifier = Modifier.width(16.dp))
            SettingsTimeWheel("Min", startM, 0..59, onStartMChange)
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // End Time Row
        Text("End Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth(), 
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SettingsTimeWheel("Hour", endH, 0..23, onEndHChange)
            Spacer(modifier = Modifier.width(16.dp))
            SettingsTimeWheel("Min", endM, 0..59, onEndMChange)
        }
    }
}

fun applyMode(context: Context, mode: ModeItem) {
    // Reset Bedtime first if switching away (unless selecting Bedtime)
    if (mode.name != "Bedtime") {
        UserPrefs.setBedtimeEnabled(context, false)
    }

    when (mode.name) {
        "Work" -> {
            UserPrefs.setGlobalIntent(context, "Work")
            UserPrefs.setGlobalStrictEnabled(context, true)
        }
        "Study" -> {
            UserPrefs.setGlobalIntent(context, "Study")
            UserPrefs.setGlobalStrictEnabled(context, false)
        }
        "Focus" -> {
            UserPrefs.setGlobalIntent(context, "Focus")
        }
        "Bedtime" -> {
            UserPrefs.setBedtimeEnabled(context, true)
        }
        "Custom" -> {
             UserPrefs.setGlobalIntent(context, "Custom")
        }
    }
}

@Composable
fun ModeCard(mode: ModeItem, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) mode.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, mode.color) else null,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = mode.icon,
                contentDescription = null,
                tint = if (isSelected) mode.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = mode.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) mode.color else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mode.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
