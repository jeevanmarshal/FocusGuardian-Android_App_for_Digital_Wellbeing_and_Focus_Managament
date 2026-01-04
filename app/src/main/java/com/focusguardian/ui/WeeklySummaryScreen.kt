package com.focusguardian.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusguardian.model.DailyUsage
import com.focusguardian.util.AnalyticsStore
import com.focusguardian.data.UsageDatabaseHelper
import com.focusguardian.data.DailyUsageStat
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.compose.ui.viewinterop.AndroidView
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale


class WeeklySummaryScreen : ComponentActivity() {

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
                WeeklySummaryContent(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklySummaryContent(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val weeklyData = remember { AnalyticsStore.getLast7Days(context) }
    
    // START: Top Apps Integration
    var topApps by remember { mutableStateOf<List<DailyUsageStat>>(emptyList()) }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = UsageDatabaseHelper(context)
            val pm = context.packageManager
            val allApps = db.getDailyUsage(UsageDatabaseHelper.getTodayKey())
            
            topApps = allApps.filter { 
                try {
                    val ai = pm.getApplicationInfo(it.packageName, 0)
                    // Exclude System Apps unless they are updated user apps
                    (ai.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (ai.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                } catch (e: Exception) {
                    false
                }
            }.sortedByDescending { it.durationMs }.take(5)
        }
    }
    // END: Top Apps Integration

    val avgScore = if (weeklyData.isNotEmpty()) {
        weeklyData.map { it.focusScore() }.average().toInt()
    } else 0


    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Weekly Insights",
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
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // AI Insight Card (Samsung Style)
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth(),
                border = CardThemeBorder()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "AI Weekly Analysis",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val insightText = when {
                        avgScore >= 80 -> "Your focus patterns are exceptional. You maintain high concentration during deep work hours. Your cognitive load is well-managed."
                        avgScore >= 60 -> "You have a solid foundation, but app switching is frequent in the evenings. Try setting a 'Social' intent for your leisure time to avoid guilt."
                        else -> "Distractions have been slightly higher this week, particularly with social apps. Consider tightening your 'Strict' alert thresholds to regain control."
                    }
                    
                    Text(
                        text = insightText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Average Score Card
            WeeklyOverviewCard(avgScore)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Focus Trends",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    FocusBarChart(weeklyData)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            // NEW: Top Apps Section
            Text(
                text = "Most Used Apps (Today)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (topApps.isNotEmpty()) {
                 Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    topApps.forEach { app ->
                        AppUsageRow(app)
                    }
                 }
            } else {
                Text(
                     "No usage data recorded today yet.", 
                     style = MaterialTheme.typography.bodyMedium, 
                     color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // History
            Text(
                text = "Past 7 Days",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            weeklyData.reversed().forEach { day ->
                DaySummaryRow(day)
            }
            
            if (weeklyData.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No data for the last 7 days", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun WeeklyOverviewCard(avgScore: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Average Focus Score",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "$avgScore%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            LinearProgressIndicator(
                progress = { avgScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun FocusBarChart(data: List<DailyUsage>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        // Last 7 slots
        val displayData = data.takeLast(7)
        
        displayData.forEach { day ->
            val score = day.focusScore()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(score / 100f)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Just first letter of day or last 2 chars of date
                Text(
                    text = day.date.takeLast(2),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        
        // Fill empty slots if less than 7 days
        repeat(7 - displayData.size) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .background(Color.DarkGray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("-", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun InsightCard(avgScore: Int) {
    val (insightText, icon) = when {
        avgScore >= 80 -> "Excellent focus! You're in the top 5% of productive users. Keep crushing your goals." to Icons.Default.AutoAwesome
        avgScore >= 60 -> "Good job! You're consistently staying focused. Small adjustments could take you even higher." to Icons.Default.AutoAwesome
        else -> "Distractions have been higher than usual this week. Check your app limits to stay on track." to Icons.Default.History
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        border = CardThemeBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = insightText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun DaySummaryRow(day: DailyUsage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = day.date, fontWeight = FontWeight.Bold)
                Text(
                    text = "${day.focusedMinutes}m focused • ${day.distractedMinutes}m distracted",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Text(
                text = "${day.focusScore()}%",
                style = MaterialTheme.typography.titleMedium,
                color = if (day.focusScore() >= 70) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun CardThemeBorder() = androidx.compose.foundation.BorderStroke(
    width = 1.dp,
    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
)

@Composable
fun AppUsageRow(stat: DailyUsageStat) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val pm = context.packageManager
    
    // Resolve Info
    var appLabel by remember { mutableStateOf(stat.packageName) }
    var appIcon by remember { mutableStateOf<Drawable?>(null) }
    
    LaunchedEffect(stat.packageName) {
        withContext(Dispatchers.IO) {
            try {
                val ai = pm.getApplicationInfo(stat.packageName, 0)
                appLabel = pm.getApplicationLabel(ai).toString()
                appIcon = pm.getApplicationIcon(ai)
            } catch (e: Exception) {
                appLabel = stat.packageName
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Real App Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = Color.Transparent
            ) {
               if (appIcon != null) {
                   AndroidView(
                       factory = { ctx ->
                           ImageView(ctx).apply {
                               setImageDrawable(appIcon)
                               scaleType = ImageView.ScaleType.FIT_CENTER
                           }
                       },
                       modifier = Modifier.fillMaxSize()
                   )
               } else {
                   Box(contentAlignment = Alignment.Center, modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha=0.2f))) {
                       Text(stat.packageName.take(1).uppercase(), color = MaterialTheme.colorScheme.primary)
                   }
               }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                 Text(
                    text = appLabel.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                 )
                 Text(
                    text = "${stat.launchCount} launches",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                 )
            }
            
            Text(
                text = "${stat.durationMs / 60000}m", // Convert ms to min
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                 color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

