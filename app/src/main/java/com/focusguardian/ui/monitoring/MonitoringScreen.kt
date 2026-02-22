package com.focusguardian.ui.monitoring

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguardian.ui.theme.FocusGuardianTheme
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.selection.selectable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusguardian.ui.AppConfigScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Divider
import androidx.compose.material3.TextButton

class MonitoringScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startTab = intent.getIntExtra("start_tab", 0)
        setContent {
            FocusGuardianTheme {
                MonitoringContent(
                    initialTab = startTab,
                    onBack = { finish() },
                    onNavigateConfig = { type, id -> 
                        val intent = Intent(this, AppConfigScreen::class.java).apply {
                            putExtra("type", type)
                            putExtra("id", id)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitoringContent(
    initialTab: Int = 0,
    onBack: () -> Unit,
    onNavigateConfig: (String, String) -> Unit,
    viewModel: MonitoringViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    // Use rememberSaveable to survive configuration changes, but initialize with initialTab
    var selectedTab by remember(initialTab) { mutableStateOf(initialTab) }
    val tabs = listOf("Apps", "Websites", "Shorts & Reels")

    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()
    
    // Enforcement Dialog State
    var showEnforcementDialog by remember { mutableStateOf(false) }
    var enforcementType by remember { mutableStateOf("") } // SHORTS or REELS
    var enforcementTarget by remember { mutableStateOf("") }

    if (showEnforcementDialog) {
        EnforcementDialog(
            type = enforcementType,
            onDismiss = { showEnforcementDialog = false },
            onConfigure = { mode ->
                showEnforcementDialog = false
                // navigate based on mode (Alert vs Block)
                // For now, we assume same config screen but maybe different params?
                // The prompt implies distinct config.
                // We will pass an extra param "mode" to config or handle logic here.
                onNavigateConfig(enforcementType, enforcementTarget + "_$mode")
            }
        )
    }

    Scaffold(
        topBar = {
            if (isSelectionMode) {
                TopAppBar(
                    title = { Text("${selectedItems.size} Selected") },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.clearSelection() }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            if(selectedTab == 0) viewModel.selectAllApps() else viewModel.selectAllSites()
                        }) {
                            Icon(Icons.Default.SelectAll, "Select All")
                        }
                        TextButton(onClick = {
                            viewModel.performBulkAction(context, "APPLY")
                            viewModel.clearSelection()
                        }) {
                            Text("PROCEED", color = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            } else {
                TopAppBar(
                    title = { Text("Monitoring Hub") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> AppMonitoringTab(state, viewModel, isSelectionMode, selectedItems, onConfig = { type, id -> onNavigateConfig(type, id) })
                1 -> WebsiteMonitoringTab(state, viewModel, isSelectionMode, selectedItems, onConfig = { type, id -> onNavigateConfig(type, id) })
                2 -> ShortsMonitoringTab(
                    onEnforcement = { type, target ->
                        enforcementType = type
                        enforcementTarget = target
                        showEnforcementDialog = true
                    },
                    onToggle = { /* handle toggle in UI */ }
                )
            }
        }
    }
}

@Composable
fun AppMonitoringTab(
    state: MonitoringUiState,
    viewModel: MonitoringViewModel,
    isSelectionMode: Boolean,
    selectedItems: Set<String>,
    onConfig: (String, String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showSortMenu by remember { mutableStateOf(false) } // Pop-up menu state
    
    val filteredApps = if (searchQuery.isEmpty()) {
        state.apps
    } else {
        state.apps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }
    
    Column {
        // Search & Sort Bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search apps...") },
                modifier = Modifier.weight(1f),
                leadingIcon = { Icon(Icons.Default.Search, null) }
            )
            Spacer(Modifier.width(8.dp))
            Box {
                IconButton(onClick = { showSortMenu = true }) { 
                    Icon(Icons.Default.Sort, "Sort") 
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    listOf("Alphabetical", "Categorized", "Most Used").forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = { 
                                viewModel.sortApps(context, option)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 CircularProgressIndicator()
             }
        } else {
            LazyColumn {
                if (state.sortOption == "Categorized") {
                    // Sorting Order: Social, Media, Payment?, System?, Entertainment?
                    // Map generic categories to these buckets for display
                    val grouped = filteredApps.groupBy { app ->
                        when(app.category) {
                            "SOCIAL" -> "Social Apps"
                            "VIDEO", "GAME" -> "Media & Entertainment"
                            "PRODUCTIVITY", "BROWSING" -> "Productivity"
                            "MESSAGING" -> "Communication"
                            else -> "Other Apps"
                        }
                    }.toSortedMap(compareBy { key ->
                        when(key) {
                            "Social Apps" -> 1
                            "Media & Entertainment" -> 2
                            "Communication" -> 3
                            "Productivity" -> 4
                            else -> 5
                        }
                    })
                    
                    grouped.forEach { (category, apps) ->
                        item {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(apps) { app ->
                            MonitoringItem(
                                id = app.packageName,
                                name = app.name,
                                packageName = app.packageName,
                                isEnabled = app.isEnabled,
                                statusInfo = app.statusInfo,
                                category = null, // Header handles it
                                isSelectionMode = isSelectionMode,
                                isSelected = selectedItems.contains(app.packageName),
                                onConfig = { onConfig("APP", app.packageName) },
                                onToggle = { enabled -> viewModel.toggleApp(context, app.packageName, enabled) },
                                onLongClick = { viewModel.toggleSelection(app.packageName) },
                                onSelectionChange = { viewModel.toggleSelection(app.packageName) }
                            )
                        }
                    }
                } else {
                    items(filteredApps) { app ->
                        MonitoringItem(
                            id = app.packageName,
                            name = app.name,
                            packageName = app.packageName,
                            isEnabled = app.isEnabled,
                            statusInfo = app.statusInfo,
                            category = app.category,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedItems.contains(app.packageName),
                            onConfig = { onConfig("APP", app.packageName) },
                            onToggle = { enabled -> viewModel.toggleApp(context, app.packageName, enabled) },
                            onLongClick = { viewModel.toggleSelection(app.packageName) },
                            onSelectionChange = { viewModel.toggleSelection(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebsiteMonitoringTab(
    state: MonitoringUiState,
    viewModel: MonitoringViewModel,
    isSelectionMode: Boolean,
    selectedItems: Set<String>,
    onConfig: (String, String) -> Unit
) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf("") }

    Column {
        // Add Site Input
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = urlInput,
                onValueChange = { urlInput = it },
                placeholder = { Text("Enter domain (e.g. facebook.com)") },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (urlInput.isNotEmpty()) {
                    viewModel.addWebsite(context, urlInput)
                    urlInput = ""
                }
            }) { Icon(Icons.Default.Add, "Add") }
        }

        LazyColumn {
            items(state.websites) { site -> 
                MonitoringItem(
                    id = site.url,
                    name = site.name,
                    packageName = null, // No package for site
                    isEnabled = site.isEnabled,
                    statusInfo = site.statusInfo,
                    isIconUrl = true, // Flag for site icon
                    isSelectionMode = isSelectionMode,
                    isSelected = selectedItems.contains(site.url),
                    onConfig = { onConfig("SITE", site.url) },
                    onToggle = { /* Toggle site logic */ },
                    onLongClick = { viewModel.toggleSelection(site.url) },
                    onSelectionChange = { viewModel.toggleSelection(site.url) }
                )
            }
        }
    }
}

@Composable
fun ShortsMonitoringTab(
    onEnforcement: (String, String) -> Unit,
    onToggle: (Boolean) -> Unit
) {
    var youtubeShortsEnabled by remember { mutableStateOf(true) }
    var instaReelsEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.padding(16.dp)) {
        MonitoringItem(
            id = "youtube_shorts",
            name = "YouTube Shorts",
            packageName = "com.google.android.youtube", // Try to get YT icon
            isEnabled = youtubeShortsEnabled,
            statusInfo = "Alert & Block Configured",
            onConfig = { onEnforcement("SHORTS", "youtube_shorts") },
            onToggle = { youtubeShortsEnabled = it },
            onLongClick = {},
            onSelectionChange = {}
        )
        Spacer(Modifier.height(8.dp))
        MonitoringItem(
            id = "insta_reels",
            name = "Instagram Reels",
            packageName = "com.instagram.android", // Try to get Insta icon
            isEnabled = instaReelsEnabled,
            statusInfo = "Alert & Block Configured",
            onConfig = { onEnforcement("REELS", "insta_reels") },
            onToggle = { instaReelsEnabled = it },
            onLongClick = {},
            onSelectionChange = {}
        )
    }
}

@Composable
fun EnforcementDialog(
    type: String,
    onDismiss: () -> Unit,
    onConfigure: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "$type Enforcement Module",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                
                // Alert Enforcement Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConfigure("ALERT") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = false, onClick = { onConfigure("ALERT") })
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Alert Enforcement", fontWeight = FontWeight.SemiBold)
                        Text(
                            "General 3-Stage Alert System (Gentle-Reminder-Strict)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Divider()
                
                // Blocking Enforcement Option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConfigure("BLOCK") }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = false, onClick = { onConfigure("BLOCK") })
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Blocking Enforcement", fontWeight = FontWeight.SemiBold)
                        Text(
                            "$type are blocked for defined time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MonitoringItem(
    id: String,
    name: String,
    packageName: String? = null,
    isEnabled: Boolean,
    statusInfo: String = "",
    category: String? = null,
    isIconUrl: Boolean = false,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onConfig: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onLongClick: () -> Unit = {},
    onSelectionChange: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Load Icon
    val iconBitmap by produceState<androidx.compose.ui.graphics.ImageBitmap?>(initialValue = null, packageName) {
        value = if (packageName != null) {
            try {
                withContext(Dispatchers.IO) {
                    val drawable = context.packageManager.getApplicationIcon(packageName)
                    drawable.toBitmap().asImageBitmap()
                }
            } catch (e: Exception) { null }
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { 
                    if (isSelectionMode) onSelectionChange() else onConfig()
                },
                onLongClick = {
                    onLongClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection Checkbox
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectionChange() }
                )
                Spacer(Modifier.width(8.dp))
            }

            // Icon
            if (iconBitmap != null) {
                Image(
                    bitmap = iconBitmap!!,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    if(isIconUrl) Icons.Default.Language else Icons.Default.Android, 
                    null, 
                    modifier = Modifier.size(40.dp), 
                    tint = Color.Gray
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (category != null) {
                    Text(category, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Light)
                }
                Text(
                    text = if(statusInfo.isNotEmpty()) statusInfo else "Gentle • Reminder • Strict", 
                    fontSize = 12.sp, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (!isSelectionMode) {
                Switch(checked = isEnabled, onCheckedChange = onToggle)
            }
        }
    }
}
