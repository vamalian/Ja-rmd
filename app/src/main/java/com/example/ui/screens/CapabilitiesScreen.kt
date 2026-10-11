package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CapabilityStatus
import com.example.data.model.DeviceCapability
import com.example.ui.theme.JarvisBlack
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisNavyElevated
import com.example.ui.theme.JarvisNavySurface
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary
import com.example.viewmodel.JarvisViewModel

@Composable
fun CapabilitiesScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capabilities by viewModel.capabilities.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("capabilities_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("capabilities_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "DEVICE CAPABILITY AUDIT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JarvisCyan
                    )
                    Text(
                        text = "Android System API Permissions & Integration Status",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextSecondary
                    )
                }
                IconButton(
                    onClick = { viewModel.refreshCapabilities() },
                    modifier = Modifier.testTag("refresh_capabilities_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = JarvisCyanLight
                    )
                }
            }

            HorizontalDivider(color = JarvisGlassBorder)

            // Permissions Quick Jump
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.deviceControlManager.openAppDetailsSettings() },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisNavyElevated, contentColor = JarvisCyan),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_system_permissions_button")
                ) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("APP PERMISSIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.deviceControlManager.openSettings("accessibility") },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisNavyElevated, contentColor = JarvisGold),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_accessibility_settings_button")
                ) {
                    Icon(imageVector = Icons.Default.AccessibilityNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ACCESSIBILITY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Capabilities List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(capabilities, key = { it.id }) { cap ->
                    CapabilityCard(cap)
                }
            }
        }
    }
}

@Composable
fun CapabilityCard(capability: DeviceCapability) {
    val (statusLabel, statusColor, statusIcon) = when (capability.status) {
        CapabilityStatus.AVAILABLE -> Triple("AVAILABLE", JarvisGreen, Icons.Default.CheckCircle)
        CapabilityStatus.PERMISSION_REQUIRED -> Triple("PERMISSION REQUIRED", JarvisGold, Icons.Default.Warning)
        CapabilityStatus.SPECIAL_ACCESS_REQUIRED -> Triple("SPECIAL ACCESS REQUIRED", JarvisGold, Icons.Default.Warning)
        CapabilityStatus.UNSUPPORTED -> Triple("UNSUPPORTED", JarvisRed, Icons.Default.Warning)
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisNavySurface.copy(alpha = 0.85f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, JarvisGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = capability.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanLight
                    )
                    Text(
                        text = "Category: ${capability.actionCategory}",
                        style = MaterialTheme.typography.labelSmall,
                        color = JarvisTextSecondary,
                        fontSize = 10.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .border(1.dp, statusColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = statusLabel,
                            tint = statusColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = statusColor,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = capability.description,
                style = MaterialTheme.typography.bodyMedium,
                color = JarvisTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(JarvisNavyElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Requirement: ${capability.permissionOrRequirement}",
                    style = MaterialTheme.typography.labelSmall,
                    color = JarvisCyanDark,
                    fontSize = 10.sp
                )
            }
        }
    }
}
