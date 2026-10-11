package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.data.model.InterfaceMode
import com.example.ui.components.ConversationPanel
import com.example.ui.components.JarvisStatusBar
import com.example.ui.components.QuickControlBar
import com.example.ui.components.TabletJarvisOrb
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisIceBlue
import com.example.ui.theme.JarvisNavyElevated
import com.example.ui.theme.JarvisNavySurface
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun JarvisTabletScreen(
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
            .testTag("jarvis_tablet_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Tablet HUD Status Bar
            JarvisStatusBar(
                telemetry = telemetry,
                assistantState = assistantState,
                currentMode = InterfaceMode.TABLET
            )

            // 2. Main 3-Pane Command Layout
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // LEFT PANE: Tactical Telemetry & Quick Action Hub
                Box(
                    modifier = Modifier
                        .weight(0.28f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisNavySurface.copy(alpha = 0.85f))
                        .border(1.dp, JarvisGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "COMMAND MATRIX",
                            style = MaterialTheme.typography.labelLarge,
                            color = JarvisCyan
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = JarvisGlassBorder
                        )

                        // Real System Telemetry Metrics
                        TabletTelemetryMetric(
                            label = "SYSTEM MEMORY",
                            value = "${telemetry.availableMemoryMb} MB AVAILABLE",
                            icon = Icons.Default.Memory,
                            tint = JarvisCyanLight
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        TabletTelemetryMetric(
                            label = "NETWORK LINK",
                            value = telemetry.networkStatus.uppercase(),
                            icon = Icons.Default.Wifi,
                            tint = if (telemetry.networkStatus != "Offline") JarvisGreen else JarvisGold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        TabletTelemetryMetric(
                            label = "ACOUSTIC LEVEL",
                            value = "${telemetry.volumeLevelPercent}% OUTPUT",
                            icon = Icons.Default.Tune,
                            tint = JarvisIceBlue
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = JarvisGlassBorder
                        )

                        Text(
                            text = "TACTICAL SHORTCUTS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = JarvisCyanDark
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            item {
                                TabletShortcutItem("YouTube", Icons.Default.PlayArrow) {
                                    viewModel.processUserInput("Open YouTube")
                                }
                            }
                            item {
                                TabletShortcutItem("Camera Optics", Icons.Default.Camera) {
                                    viewModel.processUserInput("Open Camera")
                                }
                            }
                            item {
                                TabletShortcutItem("Torch Beam", Icons.Default.FlashlightOn) {
                                    viewModel.deviceControlManager.toggleFlashlight()
                                }
                            }
                            item {
                                TabletShortcutItem("Telecom Dialer", Icons.Default.Phone) {
                                    viewModel.processUserInput("Open Dialer")
                                }
                            }
                            item {
                                TabletShortcutItem("Wi-Fi Settings", Icons.Default.Wifi) {
                                    viewModel.processUserInput("Open Wi-Fi settings")
                                }
                            }
                            item {
                                TabletShortcutItem("System Settings", Icons.Default.Settings) {
                                    viewModel.processUserInput("Open Settings")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // CENTER PANE: Cinematic Arc Reactor Core Stage
                Box(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisNavySurface.copy(alpha = 0.5f))
                        .border(1.dp, JarvisGlassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "PRIMARY REACTOR CORE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = JarvisCyanLight,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        TabletJarvisOrb(
                            state = assistantState,
                            audioAmplitude = audioAmplitude,
                            size = 280.dp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        statusBanner?.let { banner ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisNavyElevated)
                                    .border(1.dp, JarvisCyanDark.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = banner,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JarvisCyan,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // RIGHT PANE: Assistant Feed & Activity Transmission
                Box(
                    modifier = Modifier
                        .weight(0.30f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JarvisNavySurface.copy(alpha = 0.85f))
                        .border(1.dp, JarvisGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRANSMISSION FEED",
                                style = MaterialTheme.typography.labelLarge,
                                color = JarvisCyan
                            )
                            Text(
                                text = "${messages.size} ENTRIES",
                                style = MaterialTheme.typography.labelSmall,
                                color = JarvisTextSecondary
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = JarvisGlassBorder
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            ConversationPanel(
                                messages = messages,
                                pendingAction = pendingAction,
                                onConfirmAction = { viewModel.confirmPendingAction() },
                                onDismissAction = { viewModel.dismissPendingAction() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // 3. Tablet Quick Control Bar
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

@Composable
fun TabletTelemetryMetric(
    label: String,
    value: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(JarvisNavyElevated)
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisTextSecondary,
                    fontSize = 9.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = JarvisTextPrimary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun TabletShortcutItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(JarvisNavyElevated.copy(alpha = 0.7f))
            .border(1.dp, JarvisGlassBorder.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = JarvisCyan,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = JarvisTextPrimary,
            fontSize = 12.sp
        )
    }
}
