package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.data.model.InterfaceMode
import com.example.data.model.JarvisTelemetry
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisNavyElevated
import com.example.ui.theme.JarvisNavySurface
import com.example.ui.theme.JarvisRed

@Composable
fun JarvisStatusBar(
    telemetry: JarvisTelemetry,
    assistantState: AssistantState,
    currentMode: InterfaceMode,
    modifier: Modifier = Modifier
) {
    val stateLabel = when (assistantState) {
        AssistantState.IDLE -> "STANDBY"
        AssistantState.LISTENING -> "AWAITING VOCAL INPUT"
        AssistantState.THINKING -> "PROCESSING"
        AssistantState.SPEAKING -> "TRANSMITTING"
        AssistantState.EXECUTING -> "EXECUTING"
        AssistantState.ERROR -> "ATTENTION REQUIRED"
    }

    val stateColor = when (assistantState) {
        AssistantState.ERROR -> JarvisRed
        AssistantState.EXECUTING -> JarvisGold
        AssistantState.LISTENING -> JarvisCyan
        AssistantState.SPEAKING -> JarvisCyanLight
        AssistantState.THINKING -> JarvisGold
        AssistantState.IDLE -> JarvisCyanDark
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .background(JarvisNavySurface.copy(alpha = 0.85f))
            .border(1.dp, JarvisGlassBorder.copy(alpha = 0.5f), RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("jarvis_status_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: System Clock & Mode Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = telemetry.timeFormatted.ifBlank { "00:00:00" },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = JarvisCyan
                )

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(JarvisNavyElevated)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (currentMode == InterfaceMode.TABLET) "TABLET HUD" else "MOBILE HUD",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisCyanLight,
                        fontSize = 9.sp
                    )
                }

                if (telemetry.torchActive) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.FlashlightOn,
                        contentDescription = "Torch Active",
                        tint = JarvisGold,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Center: State Status Indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(stateColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stateLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = stateColor,
                    fontSize = 11.sp
                )
            }

            // Right: Network & Battery
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (telemetry.isWifi) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = JarvisCyanLight,
                        modifier = Modifier.size(15.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Network Offline/Cellular",
                        tint = if (telemetry.networkStatus.contains("Cellular")) JarvisCyanLight else JarvisRed,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = if (telemetry.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = "Battery",
                    tint = if (telemetry.isCharging) JarvisGreen else JarvisCyanLight,
                    modifier = Modifier.size(15.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = "${telemetry.batteryPercent}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = JarvisCyanLight
                )
            }
        }
    }
}
