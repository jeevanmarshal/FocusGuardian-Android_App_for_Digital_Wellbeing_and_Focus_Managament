package com.focusguardian.ui

import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.focusguardian.ui.theme.*

@Composable
fun MainContainer() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkPurple,
                contentColor = Color.White
            ) {
                val items = listOf("dashboard", "analytics", "modes", "settings")
                val icons = listOf(Icons.Default.Dashboard, Icons.Default.DateRange, Icons.Default.List, Icons.Default.Settings)
                val labels = listOf("Dashboard", "Analytics", "Modes", "Settings")

                items.forEachIndexed { index, screen ->
                    val selected = currentDestination?.route == screen
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = labels[index]) },
                        label = { Text(labels[index], fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SeedPurple,
                            selectedTextColor = SeedPurple,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextSecondaryDark,
                            indicatorColor = Color.White.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardContent(
                    onNavigateSettings = { navController.navigate("settings") },
                    onNavigateApps = { 
                         // Direct to Monitoring Hub (New screen to be created)
                         context.startActivity(Intent(context, com.focusguardian.ui.monitoring.MonitoringScreen::class.java))
                    },
                    onNavigateSummary = { navController.navigate("analytics") },
                    onNavigateSchedules = { navController.navigate("modes") },
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                )
            }
            composable("analytics") {
                // Determine if we use WeeklySummaryScreen composable or new one.
                // Using new AnalyticsContent
                com.focusguardian.ui.analytics.AnalyticsContent(
                    onBack = { navController.navigate("dashboard") }
                )
            }
            composable("modes") {
                com.focusguardian.ui.modes.ModesScreen()
            }
            composable("settings") {
                SettingsContent(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
