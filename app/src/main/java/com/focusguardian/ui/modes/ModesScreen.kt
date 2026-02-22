package com.focusguardian.ui.modes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.focusguardian.domain.logic.ModeManager
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ModesScreen(
    viewModel: ModesViewModel = viewModel()
) {
    val currentMode by viewModel.currentMode.collectAsState()

    val modes = listOf(
        ModeUiItem(ModeManager.ModeType.FOCUS, "Focus", Icons.Default.CenterFocusStrong, Color(0xFF4CAF50), "Deep work, minimize distractions"),
        ModeUiItem(ModeManager.ModeType.WORK, "Work", Icons.Default.Work, Color(0xFF2196F3), "Professional focus, strict blocks"),
        ModeUiItem(ModeManager.ModeType.STUDY, "Study", Icons.Default.School, Color(0xFFFF9800), "Learning mode, gentle reminders"),
        ModeUiItem(ModeManager.ModeType.SLEEP, "Sleep", Icons.Default.Bedtime, Color(0xFF673AB7), "Sleep protection, full blocking"),
        ModeUiItem(ModeManager.ModeType.CUSTOM, "Custom", Icons.Default.Edit, Color(0xFF9C27B0), "Personalized settings")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Select Mode",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            "Choose a mode to optimize your environment.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(modes) { modeItem ->
                ModeCard(
                    item = modeItem,
                    isActive = currentMode == modeItem.type,
                    onClick = { viewModel.activateMode(modeItem.type) }
                )
            }
        }
    }
}

data class ModeUiItem(
    val type: ModeManager.ModeType,
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeCard(item: ModeUiItem, isActive: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) item.color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isActive) androidx.compose.foundation.BorderStroke(2.dp, item.color) else null,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.height(180.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = if (isActive) item.color else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) item.color else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
