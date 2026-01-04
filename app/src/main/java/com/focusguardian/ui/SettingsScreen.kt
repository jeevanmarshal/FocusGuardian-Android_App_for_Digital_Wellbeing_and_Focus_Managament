package com.focusguardian.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.focusguardian.util.UserPrefs

class SettingsScreen : ComponentActivity() {
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
                    background = Color(0xFFF5F5F5),
                    surface = Color.White,
                    onPrimary = Color.White,
                    onSurface = Color.Black
                )
            ) {
                SettingsContent(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(onBack: () -> Unit) {
    val context = LocalContext.current
    
    var voiceEnabled by remember { mutableStateOf(UserPrefs.isVoiceAlertsEnabled(context)) }
    var voiceType by remember { mutableStateOf(UserPrefs.getAlertVoiceType(context)) }
    var strictGlobal by remember { mutableStateOf(UserPrefs.isGlobalStrictEnabled(context)) }
    var themeMode by remember { mutableStateOf(UserPrefs.getThemeMode(context)) }
    
    // Bedtime State
    var bedtimeEnabled by remember { mutableStateOf(UserPrefs.isBedtimeEnabled(context)) }
    var bedtimeStartH by remember { mutableStateOf(UserPrefs.getBedtimeStartHour(context)) }
    var bedtimeStartM by remember { mutableStateOf(UserPrefs.getBedtimeStartMinute(context)) }
    var bedtimeEndH by remember { mutableStateOf(UserPrefs.getBedtimeEndHour(context)) }
    var bedtimeEndM by remember { mutableStateOf(UserPrefs.getBedtimeEndMinute(context)) }
    
    // Time Picker State
    var showTimePicker by remember { mutableStateOf(false) }
    var pickingStart by remember { mutableStateOf(true) } // true = start, false = end


    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Settings", fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
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
            
            // General Section
            SettingsSectionTitle("General")
            
            SettingsSwitchItem(
                title = "Voice Alerts",
                subtitle = "Speak when limits are reached",
                checked = voiceEnabled,
                icon = Icons.Default.VolumeUp,
                onCheckedChange = { 
                    voiceEnabled = it
                    UserPrefs.setVoiceAlertsEnabled(context, it)
                }
            )

            if (voiceEnabled) {
                 Row(
                     modifier = Modifier.fillMaxWidth().padding(start = 56.dp, end = 16.dp, bottom = 12.dp),
                     horizontalArrangement = Arrangement.SpaceBetween,
                     verticalAlignment = Alignment.CenterVertically
                 ) {
                     Text("Voice Type", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                     Row {
                         FilterChip(
                             selected = voiceType == "FEMALE",
                             onClick = { 
                                 voiceType = "FEMALE"
                                 UserPrefs.setAlertVoiceType(context, "FEMALE")
                             },
                             label = { Text("Female") }
                         )
                         Spacer(modifier = Modifier.width(8.dp))
                         FilterChip(
                             selected = voiceType == "MALE",
                             onClick = { 
                                 voiceType = "MALE"
                                 UserPrefs.setAlertVoiceType(context, "MALE")
                             },
                             label = { Text("Male") }
                         )
                     }
                 }
            }
            
            SettingsSwitchItem(
                title = "Global Strict Mode",
                subtitle = "Enforce blocking logic globally",
                checked = strictGlobal,
                icon = Icons.Default.Security,
                onCheckedChange = { 
                    strictGlobal = it
                    UserPrefs.setGlobalStrictEnabled(context, it)
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            


            // Appearance Section
            SettingsSectionTitle("Appearance")

            SettingsClickableItem(
                title = "Theme",
                subtitle = when(themeMode) { 0 -> "System Default"; 1 -> "Light Mode"; 2 -> "Dark Mode"; else -> "System Default" },
                icon = Icons.Default.DarkMode,
                onClick = {
                    val nextMode = (themeMode + 1) % 3
                    themeMode = nextMode
                    UserPrefs.setThemeMode(context, nextMode)
                    // Activity recreation might be needed to apply theme immediately if not using reactive state for LocalTheme
                    Toast.makeText(context, "Theme updated. Restart settings to apply fully.", Toast.LENGTH_SHORT).show()
                    (context as? ComponentActivity)?.recreate()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            
            // Advanced / Debug
            SettingsSectionTitle("System")
            
            SettingsClickableItem(
                title = "Permissions Health",
                subtitle = "Check required permissions",
                icon = Icons.Default.Notifications,
                onClick = {
                    val intent = Intent(context, PermissionHealthScreen::class.java)
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
    
    if (showTimePicker) {
        val initialH = if (pickingStart) bedtimeStartH else bedtimeEndH
        val initialM = if (pickingStart) bedtimeStartM else bedtimeEndM
        
        SimpleTimePicker(
            initialHour = initialH,
            initialMinute = initialM,
            onDismiss = { showTimePicker = false },
            onTimeSet = { h, m ->
                if (pickingStart) {
                    bedtimeStartH = h
                    bedtimeStartM = m
                    UserPrefs.setBedtimeStart(context, h, m)
                } else {
                    bedtimeEndH = h
                    bedtimeEndM = m
                    UserPrefs.setBedtimeEnd(context, h, m)
                }
                showTimePicker = false
            }
        )
    }
}

@Composable
fun SimpleTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onTimeSet: (Int, Int) -> Unit
) {
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Set Time", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SettingsTimeWheel(label = "Hours", value = hour, range = 0..23) { hour = it }
                    Text(" : ", style = MaterialTheme.typography.displayMedium)
                    SettingsTimeWheel(label = "Mins", value = minute, range = 0..59) { minute = it }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "(Hrs : Minutes)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onTimeSet(hour, minute) }) { Text("OK") }
                }
            }
        }
    }
}

@Composable
fun SettingsTimeWheel(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    // Convert range to list for LazyColumn
    val items = (range).toList()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    // Initial scroll
    LaunchedEffect(value) {
        val index = items.indexOf(value)
        if (index >= 0) {
            // Center the item: visible items is usually 3-5. 
            // We want index to be in middle.
            // Simplified: snap to index
             listState.scrollToItem(index)
        }
    }
    
    // Snapping Logic
    val isScrollInProgress = listState.isScrollInProgress
    LaunchedEffect(isScrollInProgress) {
        if (!isScrollInProgress) {
            // Find visible item closest to center
            val layoutInfo = listState.layoutInfo
            val centerOffset = layoutInfo.viewportEndOffset / 2
            var closestIndex = -1
            var minDistance = Int.MAX_VALUE
            
            layoutInfo.visibleItemsInfo.forEach { item ->
                val itemCenter = item.offset + item.size / 2
                val distance = kotlin.math.abs(centerOffset - itemCenter)
                if (distance < minDistance) {
                    minDistance = distance
                    closestIndex = item.index
                }
            }
            
            if (closestIndex != -1 && closestIndex in items.indices) {
                val newValue = items[closestIndex]
                if (newValue != value) {
                    onValueChange(newValue)
                }
                listState.animateScrollToItem(closestIndex)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        androidx.compose.material3.Text(
            text = label, 
            style = MaterialTheme.typography.labelMedium, 
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier.height(120.dp).width(80.dp),
            contentAlignment = Alignment.Center
        ) {
             androidx.compose.foundation.lazy.LazyColumn(
                 state = listState,
                 contentPadding = PaddingValues(vertical = 40.dp), // Height / 3 approx
                 horizontalAlignment = Alignment.CenterHorizontally,
                 modifier = Modifier.fillMaxSize()
             ) {
                 items(items.size) { index ->
                     val itemValue = items[index]
                     val isSelected = itemValue == value
                     
                     androidx.compose.material3.Text(
                         text = "%02d".format(itemValue),
                         style = if (isSelected) 
                             MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold) 
                         else 
                             MaterialTheme.typography.titleMedium.copy(color = Color.Gray),
                         modifier = Modifier
                             .padding(vertical = 8.dp)
                             .clickable { onValueChange(itemValue) }
                     )
                 }
             }
             
             // Overlay Lines
             androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                 val strokeWidth = 2.dp.toPx()
                 val yCenter = size.height / 2
                 val offset = 25.dp.toPx()
                 
                 drawLine(
                     color = androidx.compose.ui.graphics.Color.Gray.copy(alpha=0.3f),
                     start = androidx.compose.ui.geometry.Offset(0f, yCenter - offset),
                     end = androidx.compose.ui.geometry.Offset(size.width, yCenter - offset),
                     strokeWidth = strokeWidth
                 )
                 drawLine(
                     color = androidx.compose.ui.graphics.Color.Gray.copy(alpha=0.3f),
                     start = androidx.compose.ui.geometry.Offset(0f, yCenter + offset),
                     end = androidx.compose.ui.geometry.Offset(size.width, yCenter + offset),
                     strokeWidth = strokeWidth
                 )
             }
        }
    }
}

@Composable
fun SettingsClickableItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 16.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
