package com.focusguardian.ui

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.focusguardian.util.UserPrefs
import kotlinx.coroutines.launch

class AppConfigScreen : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val pkg = intent.getStringExtra("pkg")
        val pkgs = intent.getStringArrayListExtra("pkgs")
        val appName = intent.getStringExtra("appName") ?: "App Config"
        
        if (pkg == null && (pkgs == null || pkgs.isEmpty())) {
            finish()
            return
        }

        val targetPkgs = pkgs ?: arrayListOf(pkg!!)
        val isBatch = targetPkgs.size > 1
        val displayTitle = if (isBatch) "${targetPkgs.size} Apps Selected" else appName

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
                AppConfigContent(
                    pkgs = targetPkgs,
                    appName = displayTitle,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigContent(
    pkgs: List<String>,
    appName: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val primaryPkg = pkgs.first() // For default values
    
    // Helper to format seconds to H:M:S string
    fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // State (Load from primaryPkg initially)
    var gentleSeconds by remember { mutableStateOf(UserPrefs.getGentleSeconds(context, primaryPkg)) }
    var reminderSeconds by remember { mutableStateOf(UserPrefs.getReminderSeconds(context, primaryPkg)) }
    var strictSeconds by remember { mutableStateOf(UserPrefs.getStrictSeconds(context, primaryPkg)) }
    
    var gentleMsg by remember { mutableStateOf(UserPrefs.getGentleMessage(context, primaryPkg)) }
    var reminderMsg by remember { mutableStateOf(UserPrefs.getReminderMessage(context, primaryPkg)) }
    var strictMsg by remember { mutableStateOf(UserPrefs.getStrictMessage(context, primaryPkg)) }
    var pendingTask by remember { mutableStateOf(UserPrefs.getPendingTask(context, primaryPkg)) }
    
    var isGentleEnabled by remember { mutableStateOf(UserPrefs.isGentleEnabled(context, primaryPkg)) }
    var isReminderEnabled by remember { mutableStateOf(UserPrefs.isReminderEnabled(context, primaryPkg)) }
    var isStrictEnabled by remember { mutableStateOf(UserPrefs.isStrictEnabled(context, primaryPkg)) }
    var isEmergencyAllowed by remember { mutableStateOf(UserPrefs.isEmergencyUnlockAllowed(context, primaryPkg)) }
    
    var appIntent by remember { mutableStateOf(UserPrefs.getAppIntent(context, primaryPkg)) }
    var isAIAdaptive by remember { mutableStateOf(UserPrefs.isAIAdaptiveEnabled(context, primaryPkg)) }
    var sensitivity by remember { mutableStateOf(UserPrefs.getCognitiveSensitivity(context, primaryPkg).toFloat()) }
    var pauseDuration by remember { mutableStateOf(UserPrefs.getPauseDurationMinutes(context, primaryPkg)) }

    // Time Picker States
    var showTimePickerFor by remember { mutableStateOf<String?>(null) } // "GENTLE", "REMINDER", "STRICT"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        appName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Save to ALL packages
                    pkgs.forEach { p ->
                        UserPrefs.setGentleSeconds(context, p, gentleSeconds)
                        UserPrefs.setReminderSeconds(context, p, reminderSeconds)
                        UserPrefs.setStrictSeconds(context, p, strictSeconds)

                        UserPrefs.setGentleMessage(context, p, gentleMsg)
                        UserPrefs.setReminderMessage(context, p, reminderMsg)
                        UserPrefs.setStrictMessage(context, p, strictMsg)
                        UserPrefs.setPendingTask(context, p, pendingTask)

                        UserPrefs.setGentleEnabled(context, p, isGentleEnabled)
                        UserPrefs.setReminderEnabled(context, p, isReminderEnabled)
                        UserPrefs.setStrictEnabled(context, p, isStrictEnabled)
                        UserPrefs.setEmergencyUnlockAllowed(context, p, isEmergencyAllowed)
                        
                        UserPrefs.setAppIntent(context, p, appIntent)
                        UserPrefs.setAIAdaptiveEnabled(context, p, isAIAdaptive)
                        UserPrefs.setCognitiveSensitivity(context, p, sensitivity.toInt())
                        UserPrefs.setPauseDurationMinutes(context, p, pauseDuration)
                    }

                    Toast.makeText(context, "Settings saved for ${pkgs.size} apps", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save All")
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Time Thresholds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TimeInputItem(
                        label = "Gentle Alert", 
                        timeStr = formatTime(gentleSeconds),
                        onClick = { showTimePickerFor = "GENTLE" }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TimeInputItem(
                        label = "Reminder Alert", 
                        timeStr = formatTime(reminderSeconds),
                        onClick = { showTimePickerFor = "REMINDER" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TimeInputItem(
                        label = "Strict Block", 
                        timeStr = formatTime(strictSeconds),
                        onClick = { showTimePickerFor = "STRICT" }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Custom Alert Messages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = gentleMsg,
                        onValueChange = { gentleMsg = it },
                        label = { Text("Gentle Alert Message") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reminderMsg,
                        onValueChange = { reminderMsg = it },
                        label = { Text("Reminder Alert Message") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = strictMsg,
                        onValueChange = { strictMsg = it },
                        label = { Text("Strict Alert Message") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pendingTask,
                        onValueChange = { pendingTask = it },
                        label = { Text("Pending Task") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            // ... (Rest of existing UI logic maintained mostly for Intent/Enforcement)
            
             Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Enforcement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    SettingsSwitchItem(
                        title = "Gentle Alert",
                        subtitle = "First soft warning",
                        checked = isGentleEnabled,
                        onCheckedChange = { isGentleEnabled = it }
                    )

                    SettingsSwitchItem(
                        title = "Reminder Alert",
                        subtitle = "Secondary nudge",
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it }
                    )

                    SettingsSwitchItem(
                        title = "Strict Enforcement",
                        subtitle = "Force exit app & block",
                        checked = isStrictEnabled,
                        onCheckedChange = { isStrictEnabled = it }
                    )

                    if (isStrictEnabled) {
                        TimeInputItem(
                            label = "Block Duration", 
                            timeStr = formatTime(pauseDuration * 60L),
                            onClick = { showTimePickerFor = "PAUSE_DURATION" }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    SettingsSwitchItem(
                        title = "Emergency Unlock",
                        subtitle = "Allow manual override",
                        checked = isEmergencyAllowed,
                        onCheckedChange = { isEmergencyAllowed = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))
            
            // Manual Override Section
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha=0.1f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha=0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Manual Override",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "If this app is currently blocked and you need urgent access, you can manually unblock it here. This will reset the current strict block timer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            pkgs.forEach { p ->
                                // Reset pause duration (unblock)
                                UserPrefs.setAppPausedUntil(context, p, 0)
                            }
                            Toast.makeText(context, "App Unblocked. You can now access it.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unblock App Now")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (showTimePickerFor != null) {
        val currentSeconds = when (showTimePickerFor) {
            "GENTLE" -> gentleSeconds
            "REMINDER" -> reminderSeconds
            "STRICT" -> strictSeconds
            "PAUSE_DURATION" -> pauseDuration * 60L
            else -> 0L
        }

        ClockSpinnerOverlay(
            initialSeconds = currentSeconds,
            onDismiss = { showTimePickerFor = null },
            onTimeSet = { newSeconds ->
                when (showTimePickerFor) {
                    "GENTLE" -> gentleSeconds = newSeconds
                    "REMINDER" -> reminderSeconds = newSeconds
                    "STRICT" -> strictSeconds = newSeconds
                    "PAUSE_DURATION" -> pauseDuration = (newSeconds / 60).toInt()
                }
                showTimePickerFor = null
            }
        )
    }
}

@Composable
fun TimeInputItem(label: String, timeStr: String, onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = timeStr,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )
    }
}

// ClockSpinnerOverlay implementation
@Composable
fun ClockSpinnerOverlay(
    initialSeconds: Long,
    onDismiss: () -> Unit,
    onTimeSet: (Long) -> Unit
) {
    var hours by remember { mutableStateOf((initialSeconds / 3600).toInt()) }
    var minutes by remember { mutableStateOf(((initialSeconds % 3600) / 60).toInt()) }
    var seconds by remember { mutableStateOf((initialSeconds % 60).toInt()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Set Duration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TimeWheel(label = "Hours", value = hours, range = 0..23) { hours = it }
                        Text(":", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        TimeWheel(label = "Mins", value = minutes, range = 0..59) { minutes = it }
                        Text(":", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                        TimeWheel(label = "Secs", value = seconds, range = 0..59) { seconds = it }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "(Hrs : Minutes : Seconds)",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedButton(
                            onClick = {
                                hours = 0
                                minutes = 0
                                seconds = 0
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reset")
                        }
                        Button(
                            onClick = {
                                val total = (hours * 3600L) + (minutes * 60L) + seconds
                                onTimeSet(total)
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Set Time")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeWheel(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    val items = (range).toList()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    LaunchedEffect(value) {
        val index = items.indexOf(value)
        if (index >= 0) {
             listState.scrollToItem(index)
        }
    }
    
    val isScrollInProgress = listState.isScrollInProgress
    LaunchedEffect(isScrollInProgress) {
        if (!isScrollInProgress) {
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
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier.height(120.dp).width(80.dp),
            contentAlignment = Alignment.Center
        ) {
             androidx.compose.foundation.lazy.LazyColumn(
                 state = listState,
                 contentPadding = PaddingValues(vertical = 40.dp),
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
                             MaterialTheme.typography.headlineMedium.copy(color = Color.Gray),
                         color = if(isSelected) Color.White else Color.Gray,
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

