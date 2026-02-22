package com.focusguardian.ui.monitoring

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusguardian.util.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MonitoredApp(
    val packageName: String,
    val name: String,
    val isEnabled: Boolean,
    val category: String,
    val statusInfo: String
)

data class MonitoredSite(
    val url: String,
    val name: String,
    val isEnabled: Boolean,
    val statusInfo: String
)

data class MonitoringUiState(
    val apps: List<MonitoredApp> = emptyList(),
    val websites: List<MonitoredSite> = emptyList(),
    val isLoading: Boolean = false,
    val sortOption: String = "Alphabetical" // Alphabetical, Categorized, Most Used
)

class MonitoringViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState

    private val _selectedItems = MutableStateFlow<Set<String>>(emptySet())
    val selectedItems: StateFlow<Set<String>> = _selectedItems

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    fun loadApps(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // Apps
            val apps = withContext(Dispatchers.IO) {
                getInstalledApps(context)
            }
            
            // Sites (Mock or Prefs)
            val sites = loadWebsites(context)
            
            _uiState.value = _uiState.value.copy(apps = apps, websites = sites, isLoading = false)
            sortApps(context, _uiState.value.sortOption)
        }
    }

    private fun getInstalledApps(context: Context): List<MonitoredApp> {
        val pm = context.packageManager
        val allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        return allApps
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (it.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 } // Include user-installed or updated system apps
            .map {
                val isMonitored = UserPrefs.isAppMonitored(context, it.packageName)
                val category = com.focusguardian.util.AppCategoryResolver.resolve(context, it.packageName).name
                
                // Status Info: [Gentle-Reminder-Strict]
                val gentle = if(UserPrefs.isGentleEnabled(context, it.packageName)) "Gentle" else ""
                val reminder = if(UserPrefs.isReminderEnabled(context, it.packageName)) "Reminder" else ""
                val strict = if(UserPrefs.isStrictEnabled(context, it.packageName)) "Strict" else ""
                val statusList = listOf(gentle, reminder, strict).filter { s -> s.isNotEmpty() }
                val status = if (statusList.isNotEmpty()) statusList.joinToString("-") else ""
                
                MonitoredApp(
                    packageName = it.packageName,
                    name = pm.getApplicationLabel(it).toString(),
                    isEnabled = isMonitored,
                    category = category,
                    statusInfo = if(status.isEmpty()) "Not Configured" else "[$status]"
                )
            }
    }
    
    private fun loadWebsites(context: Context): List<MonitoredSite> {
        val sites = UserPrefs.getMonitoredSites(context)
        return sites.map { domain ->
            val isEnabled = UserPrefs.isSiteEnabled(context, domain)
            
            val gentle = if(UserPrefs.isGentleEnabled(context, domain)) "Gentle" else ""
            val reminder = if(UserPrefs.isReminderEnabled(context, domain)) "Reminder" else ""
            val strict = if(UserPrefs.isStrictEnabled(context, domain)) "Strict" else ""
            val statusList = listOf(gentle, reminder, strict).filter { s -> s.isNotEmpty() }
            val status = if (statusList.isNotEmpty()) statusList.joinToString("-") else ""
            
            MonitoredSite(
                url = domain,
                name = domain, // Ideally fetch title, but using domain for now
                isEnabled = isEnabled,
                statusInfo = if(status.isEmpty()) "Standard" else "[$status]"
            )
        }.sortedBy { it.name }
    }
    
    fun addWebsite(context: Context, url: String) {
        UserPrefs.addMonitoredSite(context, url)
        // Refresh list
        val sites = loadWebsites(context)
        _uiState.value = _uiState.value.copy(websites = sites)
    }

    fun toggleApp(context: Context, packageName: String, enabled: Boolean) {
        UserPrefs.setAppMonitored(context, packageName, enabled)
        val updatedApps = _uiState.value.apps.map {
            if (it.packageName == packageName) it.copy(isEnabled = enabled) else it
        }
        _uiState.value = _uiState.value.copy(apps = updatedApps)
    }

    fun sortApps(context: Context, sortOption: String) {
        val currentApps = _uiState.value.apps
        val sorted = when(sortOption) {
            "Alphabetical" -> currentApps.sortedBy { it.name }
            "Categorized" -> currentApps.sortedWith(compareBy({ it.category }, { it.name }))
            "Most Used" -> currentApps // Placeholder for Most Used
            else -> currentApps.sortedBy { it.name }
        }
        _uiState.value = _uiState.value.copy(apps = sorted, sortOption = sortOption)
    }

    // Selection Logic
    fun toggleSelection(id: String) {
        val current = _selectedItems.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedItems.value = current
        if (current.isEmpty()) {
            _isSelectionMode.value = false
        } else {
            _isSelectionMode.value = true
        }
    }

    fun selectAllApps() {
        val allIds = _uiState.value.apps.map { it.packageName }.toSet()
        _selectedItems.value = allIds
        _isSelectionMode.value = true
    }
    
    fun selectAllSites() {
        val allIds = _uiState.value.websites.map { it.url }.toSet()
        _selectedItems.value = allIds
        _isSelectionMode.value = true
    }

    fun clearSelection() {
        _selectedItems.value = emptySet()
        _isSelectionMode.value = false
    }
    
    fun performBulkAction(context: Context, action: String) {
        // Implement bulk actions here (e.g., Enable All, Disable All)
        // For now, just clear selection
        clearSelection()
    }
}
