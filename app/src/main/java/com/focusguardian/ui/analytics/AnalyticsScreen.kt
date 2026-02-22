package com.focusguardian.ui.analytics

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguardian.ui.theme.FocusGuardianTheme
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusGuardianTheme {
                AnalyticsContent(onBack = { finish() })
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsContent(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedModule by remember { mutableStateOf("Usage Time") }

    val dateFormatter = SimpleDateFormat("dd-MM-yyyy | EEEE", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Date Picker
            val calendar = state.selectedDate
            val context = LocalContext.current
            
            val datePickerDialog = android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val newCal = Calendar.getInstance()
                    newCal.set(year, month, dayOfMonth)
                    viewModel.onDateSelected(newCal)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFormatter.format(state.selectedDate.time),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { datePickerDialog.show() }) {
                         Icon(Icons.Default.CalendarToday, "Calendar")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 2. Changeable Modules
            val modules = listOf("Usage Time", "App Launches", "Usage Timeline")
            TabRow(
                selectedTabIndex = modules.indexOf(selectedModule),
                containerColor = Color.Transparent
            ) {
                modules.forEach { title ->
                    Tab(
                        selected = selectedModule == title,
                        onClick = { selectedModule = title },
                        text = { Text(title, fontSize = 12.sp, maxLines = 1) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // 3. Module Content
                when (selectedModule) {
                    "Usage Time" -> UsageTimeModule(state.selectedDate, state.usageList)
                    "App Launches" -> AppLaunchesModule(state.selectedDate, state.usageList)
                    "Usage Timeline" -> UsageTimelineModule(state.selectedDate)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageTimeModule(date: Calendar, usageList: List<com.focusguardian.data.local.entity.AppUsageEntity>) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    // Filter Tabs (All | Apps | Sites | Shorts)
    var filter by remember { mutableStateOf("All") }
    
    val filteredList = usageList.filter { usage ->
        when (filter) {
            "Apps" -> !usage.packageName.startsWith("site:") && usage.packageName != "youtube_shorts" && usage.packageName != "insta_reels"
            "Sites" -> usage.packageName.startsWith("site:")
            "Shorts" -> usage.packageName == "youtube_shorts" || usage.packageName == "insta_reels"
            else -> true
        }
    }

    Column {
        // Total Time Graph based on filtered list
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            val totalMillis = filteredList.sumOf { it.usageDurationMillis }
            val hrs = totalMillis / 3600000
            val mins = (totalMillis % 3600000) / 60000
            val secs = (totalMillis % 60000) / 1000
            Text("Total: ${hrs}h ${mins}m ${secs}s", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text("Usage Stats", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("All", "Apps", "Sites", "Shorts").forEach { 
                FilterChip(
                    selected = filter == it,
                    onClick = { filter = it },
                    label = { Text(it) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        filteredList.sortedByDescending { it.usageDurationMillis }.forEach { usage ->
            val appLabel = try {
                if (usage.packageName == "youtube_shorts") "YouTube Shorts"
                else if (usage.packageName == "insta_reels") "Instagram Reels"
                else if (usage.packageName.startsWith("site:")) usage.packageName.removePrefix("site:")
                else pm.getApplicationLabel(pm.getApplicationInfo(usage.packageName, 0)).toString()
            } catch (e: Exception) {
                usage.packageName
            }
            
            val hrs = usage.usageDurationMillis / 3600000
            val mins = (usage.usageDurationMillis % 3600000) / 60000
            val secs = (usage.usageDurationMillis % 60000) / 1000
            
            Row(
                 modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                 verticalAlignment = Alignment.CenterVertically
            ) {
                 // Icon Placeholder - Improved
                 val iconVector = when {
                    usage.packageName == "youtube_shorts" -> Icons.Default.PlayCircle
                    usage.packageName == "insta_reels" -> Icons.Default.PlayCircle
                    usage.packageName.startsWith("site:") -> Icons.Default.Public
                    else -> Icons.Default.Android
                 }
                 
                Icon(imageVector = iconVector, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(appLabel, fontWeight = FontWeight.Bold)
                }
                Text("${String.format("%02d", hrs)} hr ${String.format("%02d", mins)} min ${String.format("%02d", secs)} sec", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLaunchesModule(date: Calendar, usageList: List<com.focusguardian.data.local.entity.AppUsageEntity>) {
    val context = LocalContext.current
    val pm = context.packageManager

    // Filter Tabs (All | Apps | Sites | Shorts)
    var filter by remember { mutableStateOf("All") }
    
    val filteredList = usageList.filter { usage ->
        when (filter) {
            "Apps" -> !usage.packageName.startsWith("site:") && usage.packageName != "youtube_shorts" && usage.packageName != "insta_reels"
            "Sites" -> usage.packageName.startsWith("site:")
            "Shorts" -> usage.packageName == "youtube_shorts" || usage.packageName == "insta_reels"
            else -> true
        }
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.DarkGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            val totalLaunches = filteredList.sumOf { it.openCount }
            Text("Total Launches: $totalLaunches", color = Color.White, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            listOf("All", "Apps", "Sites", "Shorts").forEach { 
                FilterChip(
                    selected = filter == it,
                    onClick = { filter = it },
                    label = { Text(it) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        filteredList.sortedByDescending { it.openCount }.forEach { usage ->
            val appLabel = try {
                 if (usage.packageName == "youtube_shorts") "YouTube Shorts"
                else if (usage.packageName == "insta_reels") "Instagram Reels"
                else if (usage.packageName.startsWith("site:")) usage.packageName.removePrefix("site:")
                else pm.getApplicationLabel(pm.getApplicationInfo(usage.packageName, 0)).toString()
            } catch (e: Exception) {
                usage.packageName
            }

             Row(
                 modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                 verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Placeholder - Improved
                 val iconVector = when {
                    usage.packageName == "youtube_shorts" -> Icons.Default.PlayCircle
                    usage.packageName == "insta_reels" -> Icons.Default.PlayCircle
                    usage.packageName.startsWith("site:") -> Icons.Default.Public
                    else -> Icons.Default.Android
                 }
                 Icon(imageVector = iconVector, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Gray)
                 
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(appLabel, fontWeight = FontWeight.Bold)
                }
                Text("${usage.openCount} Launches", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun UsageTimelineModule(date: Calendar) {
    Column {
         Text("Timeline", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
         Spacer(modifier = Modifier.height(16.dp))
         
         // Timeline List
         repeat(5) {
             Row(
                 modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
             ) {
                 // Time Column
                 Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                     Text("10:${it}0", fontWeight = FontWeight.Bold)
                     Box(Modifier.width(2.dp).height(40.dp).background(Color.Gray))
                 }
                 
                 // Content
                 Card(
                     modifier = Modifier.weight(1f),
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                 ) {
                     Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                         Box(Modifier.size(32.dp).background(Color.Gray, androidx.compose.foundation.shape.CircleShape))
                         Spacer(Modifier.width(12.dp))
                         Column {
                             Text("Instagram", fontWeight = FontWeight.Bold)
                             Text("Used for 15 mins", fontSize = 12.sp, color = Color.Gray)
                         }
                     }
                 }
             }
         }
    }
}
