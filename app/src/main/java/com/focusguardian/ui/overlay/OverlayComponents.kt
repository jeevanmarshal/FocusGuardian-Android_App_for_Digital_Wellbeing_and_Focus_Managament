package com.focusguardian.ui.overlay

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// import com.focusguardian.R // Ensure R is generated or handle dynamic resources

@Composable
fun GentleAlertOverlay(
    appName: String,
    customMessage: String,
    onOkClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.padding(16.dp).fillMaxWidth().wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo placeholder (Dynamic loading would require Coil, using simple Text for now or Icon)
            Text(text = "Focus Guardian", fontSize = 12.sp, color = Color.Gray)
            Text(text = appName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Gentle Alert", color = Color(0xFFFFA500), fontWeight = FontWeight.SemiBold) // Orange
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have reached the time limit of $appName.\n$customMessage",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onOkClick, modifier = Modifier.weight(1f)) {
                    Text("Ok")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onExitClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Exit")
                }
            }
        }
    }
}

@Composable
fun ReminderAlertOverlay(
    appName: String,
    customMessage: String,
    onOkClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.padding(16.dp).fillMaxWidth().wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = appName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Reminder Alert", color = Color(0xFFFF4500), fontWeight = FontWeight.SemiBold) // Red-Orange
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have reached the time limit of $appName.\n$customMessage",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onOkClick, modifier = Modifier.weight(1f)) {
                    Text("Ok")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onExitClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Exit")
                }
            }
        }
    }
}

@Composable
fun StrictBlockingOverlay(
    appName: String,
    customMessage: String,
    timeLeftSeconds: Long, // 10 -> 0
    onCloseClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)), // Light Red
        modifier = Modifier.padding(16.dp).fillMaxWidth().wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = appName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Strict Blocking", color = Color.Red, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Time's Up!", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red)
            Text(
                text = "You have reached the time limit of $appName.\n$customMessage",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Closing in $timeLeftSeconds",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCloseClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Close App")
            }
        }
    }
}

@Composable
fun AccessBlockedOverlay(
    appName: String,
    pendingTask: String,
    onUnlockClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.padding(16.dp).fillMaxWidth().wrapContentHeight()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = appName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "App Access Blocked", color = Color.Red, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Proceed to Pending task: $pendingTask",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onUnlockClick,
                    modifier = Modifier.weight(1f),
                   colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Unlock")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onExitClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Exit")
                }
            }
        }
    }
}
