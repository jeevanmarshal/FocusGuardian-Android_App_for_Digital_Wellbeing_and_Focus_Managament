package com.focusguardian.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.focusguardian.ServiceLocator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.Divider // Add Divider import
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
            // Note: FocusGuardianTheme handles theming internally based on system/user pref if we wired it up, 
            // but here we manually pass `darkTheme` based on UserPrefs.
            // FocusGuardianTheme expects `darkTheme: Boolean`.
            val isDark = when (themeMode) {
                1 -> false
                2 -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            com.focusguardian.ui.theme.FocusGuardianTheme(darkTheme = isDark) {
                SettingsContent(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(onBack: () -> Unit) {
    val context = LocalContext.current
    
    // ServiceLocator Access
    // Using new Managers directly via userPrefs or simple logic since complex controllers were removed
    // Ideally we'd use a ViewModel but for this cleanup phase we stick to inline logic
    
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

            // Emergency Override Section
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Emergency Access",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap to immediately unblock all apps and disable strict mode if you are stuck.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // Emergency Logic
                            UserPrefs.setGlobalStrictEnabled(context, false)
                            strictGlobal = false
                            
                            // Hide overlays
                            try {
                                ServiceLocator.overlayController.hideOverlay()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            
                            // Reset all blocks (Placeholder call, ideally iterate pkgs or clear flag)
                            // Since we can't iterate all, disabling strict/overlay is the main step.
                            
                            Toast.makeText(context, "Emergency Unlock Activated", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Manual Override / Unblock All")
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            


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

            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
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




