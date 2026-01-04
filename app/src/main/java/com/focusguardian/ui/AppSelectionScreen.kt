package com.focusguardian.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.focusguardian.util.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AppItem(
    val name: String,
    val pkg: String,
    val icon: Drawable,
    var isMonitored: Boolean, // Mutable to reflect immediate changes
    val category: com.focusguardian.logic.AppCategory
)

class AppSelectionScreen : ComponentActivity() {
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
                AppSelectionContent(
                    onBack = { finish() },
                    onConfigureApps = { pkgs, appName ->
                        val intent = Intent(this, AppConfigScreen::class.java).apply {
                            putStringArrayListExtra("pkgs", ArrayList(pkgs))
                            putExtra("appName", appName)
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
fun AppSelectionContent(
    onBack: () -> Unit,
    onConfigureApps: (List<String>, String) -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var showSystemApps by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf("Alphabetical") }
    var showSortMenu by remember { mutableStateOf(false) }

    // Multi-Selection State
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedPkgs by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(showSystemApps, sortOption) {
        isLoading = true
        withContext(Dispatchers.IO) {
            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val appList = installed.mapNotNull { app ->
                if (app.packageName == context.packageName) return@mapNotNull null
                val launchIntent = pm.getLaunchIntentForPackage(app.packageName)
                if (showSystemApps || launchIntent != null) {
                    AppItem(
                        name = app.loadLabel(pm).toString(),
                        pkg = app.packageName,
                        icon = app.loadIcon(pm),
                        isMonitored = UserPrefs.isAppMonitored(context, app.packageName),
                        category = com.focusguardian.logic.AppCategoryResolver.resolve(context, app.packageName)
                    )
                } else null
            }
            
            val sortedList = when (sortOption) {
                "Alphabetical" -> appList.sortedBy { it.name.lowercase() }
                "Most Used" -> appList.sortedByDescending { UserPrefs.getAppUsageCount(context, it.pkg) }
                "Last Used" -> appList.sortedByDescending { UserPrefs.getLastUsedTimestamp(context, it.pkg) }
                else -> appList.sortedBy { it.name.lowercase() }
            }
            
            withContext(Dispatchers.Main) {
                apps = sortedList
                isLoading = false
            }
        }
    }

    val filteredApps = apps.filter { 
        it.name.contains(searchQuery, ignoreCase = true) || it.pkg.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text("${selectedPkgs.size} Selected")
                    } else {
                        Text("Monitor Apps", fontWeight = FontWeight.Bold) 
                    }
                },
                navigationIcon = {
                    if (isSelectionMode) {
                        IconButton(onClick = { 
                            isSelectionMode = false 
                            selectedPkgs = emptySet()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Selection")
                        }
                    } else {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    // Always show sort menu, even in selection mode
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            listOf("Alphabetical", "Most Used", "Last Used").forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        sortOption = opt
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (isSelectionMode && selectedPkgs.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = {
                        onConfigureApps(selectedPkgs.toList(), "Multiple Apps")
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Configure Selected")
                }
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            
            if (!isSelectionMode) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        placeholder = { Text("Search apps...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Text(
                        "Long-press to select multiple apps and apply the same settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp)
                    )
                }
            } else {
                Text(
                    "Select apps to configure together",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
                ) {
                    items(filteredApps, key = { it.pkg }) { app ->
                        val isSelected = selectedPkgs.contains(app.pkg)
                        var isMonitored by remember { mutableStateOf(app.isMonitored) }

                        ListItem(
                            headlineContent = { Text(app.name, fontWeight = FontWeight.Bold) },
                            leadingContent = {
                                Box {
                                    Surface(
                                        modifier = Modifier.size(50.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Image(
                                            bitmap = app.icon.toBitmap().asImageBitmap(),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().padding(4.dp)
                                        )
                                    }
                                    if (isSelectionMode && isSelected) {
                                        Surface(
                                            modifier = Modifier.matchParentSize(),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                                        }
                                    }
                                }
                            },
                            trailingContent = {
                                if (!isSelectionMode) {
                                    Switch(
                                        checked = isMonitored,
                                        onCheckedChange = { 
                                            isMonitored = it
                                            UserPrefs.setAppMonitored(context, app.pkg, it)
                                            UserPrefs.setAppName(context, app.pkg, app.name)
                                        }
                                    )
                                } else {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedPkgs = if (checked) selectedPkgs + app.pkg else selectedPkgs - app.pkg
                                        }
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            if (!isSelectionMode) {
                                                isSelectionMode = true
                                                selectedPkgs = selectedPkgs + app.pkg
                                            }
                                        },
                                        onTap = {
                                            if (isSelectionMode) {
                                                selectedPkgs = if (isSelected) selectedPkgs - app.pkg else selectedPkgs + app.pkg
                                            } else {
                                                onConfigureApps(listOf(app.pkg), app.name)
                                            }
                                        }
                                    )
                                },
                            colors = ListItemDefaults.colors(containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color.Gray.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}
