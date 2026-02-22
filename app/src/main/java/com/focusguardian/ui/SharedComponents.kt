package com.focusguardian.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        ),
        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
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
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun SettingsTimeWheel(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    // Reusing the same implementation as seen in SettingsScreen
    // Logic: LazyColumn with snapping
    val items = (range).toList()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    
    androidx.compose.runtime.LaunchedEffect(value) {
        val index = items.indexOf(value)
        if (index >= 0) {
             listState.scrollToItem(index)
        }
    }
    
    val isScrollInProgress = listState.isScrollInProgress
    androidx.compose.runtime.LaunchedEffect(isScrollInProgress) {
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                             MaterialTheme.typography.titleMedium.copy(color = androidx.compose.ui.graphics.Color.Gray),
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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
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
