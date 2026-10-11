package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.data.model.InterfaceMode
import com.example.ui.components.ConversationPanel
import com.example.ui.components.JarvisStatusBar
import com.example.ui.components.MobileJarvisOrb
import com.example.ui.components.QuickControlBar
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisNavyElevated
import com.example.ui.theme.JarvisNavySurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun JarvisMobileScreen(
    viewModel: JarvisViewModel,
    onOpenSettings: () -> Unit,
    onOpenCapabilities: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assistantState by viewModel.assistantState.collectAsState()
    val audioAmplitude by viewModel.audioAmplitude.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val pendingAction by viewModel.pendingAction.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("jarvis_mobile_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 1. Status Bar
            JarvisStatusBar(
                telemetry = telemetry,
                assistantState = assistantState,
                currentMode = InterfaceMode.MOBILE
            )

            // 2. Status Banner / Transmission Notification
            statusBanner?.let { banner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(JarvisNavyElevated.copy(alpha = 0.7f))
                        .border(1.dp, JarvisGlassBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "› $banner",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisCyanLight,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }

            // 3. Central Energy Core (Mobile Arc-Reactor Orb)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                MobileJarvisOrb(
                    state = assistantState,
                    audioAmplitude = audioAmplitude,
                    size = 190.dp
                )
            }

            // 4. Quick Telemetry & Hardware Control Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Torch toggle chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (telemetry.torchActive) JarvisGold.copy(alpha = 0.2f) else JarvisNavySurface)
                        .border(1.dp, if (telemetry.torchActive) JarvisGold else JarvisGlassBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.deviceControlManager.toggleFlashlight() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("quick_torch_toggle"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (telemetry.torchActive) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = "Torch",
                        tint = if (telemetry.torchActive) JarvisGold else JarvisTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (telemetry.torchActive) "TORCH ON" else "TORCH",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (telemetry.torchActive) JarvisGold else JarvisTextSecondary,
                        fontSize = 10.sp
                    )
                }

                // Volume Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(JarvisNavySurface)
                        .border(1.dp, JarvisGlassBorder, RoundedCornerShape(20.dp))
                        .clickable { viewModel.deviceControlManager.adjustVolume(1) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Volume",
                        tint = JarvisCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VOL: ${telemetry.volumeLevelPercent}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisCyanLight,
                        fontSize = 10.sp
                    )
                }

                // RAM Chip
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(JarvisNavySurface)
                        .border(1.dp, JarvisGlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Memory",
                        tint = JarvisCyanLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${telemetry.availableMemoryMb}MB RAM",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Conversation Stream
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
            ) {
                ConversationPanel(
                    messages = messages,
                    pendingAction = pendingAction,
                    onConfirmAction = { viewModel.confirmPendingAction() },
                    onDismissAction = { viewModel.dismissPendingAction() },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 6. Bottom Controls
            QuickControlBar(
                assistantState = assistantState,
                spokenResponsesEnabled = settings.spokenResponsesEnabled,
                onStartListening = { viewModel.startListening() },
                onStopListening = { viewModel.stopListening() },
                onStopSpeaking = { viewModel.stopSpeaking() },
                onToggleSpokenResponses = { viewModel.setSpokenResponses(!settings.spokenResponsesEnabled) },
                onSendText = { viewModel.processUserInput(it) },
                onOpenSettings = onOpenSettings,
                onOpenCapabilities = onOpenCapabilities
            )
        }
    }
}
