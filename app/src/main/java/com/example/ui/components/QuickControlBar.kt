package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AssistantState
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisNavyElevated
import com.example.ui.theme.JarvisNavySurface
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun QuickControlBar(
    assistantState: AssistantState,
    spokenResponsesEnabled: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onStopSpeaking: () -> Unit,
    onToggleSpokenResponses: () -> Unit,
    onSendText: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenCapabilities: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }
    val isListening = assistantState == AssistantState.LISTENING
    val isSpeaking = assistantState == AssistantState.SPEAKING

    val pulseTransition = rememberInfiniteTransition(label = "MicPulse")
    val micScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MicScale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(JarvisNavySurface.copy(alpha = 0.95f))
            .border(1.dp, JarvisGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("quick_control_bar")
    ) {
        // Text Command Input Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = {
                    Text(
                        "Command J.A.R.V.I.S. (e.g. Open YouTube, Flashlight)...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextSecondary.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (textInput.isNotBlank()) {
                        onSendText(textInput)
                        textInput = ""
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisGlassBorder,
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisNavyElevated,
                    unfocusedContainerColor = JarvisNavyElevated
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("command_input_field")
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendText(textInput)
                        textInput = ""
                    }
                },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = JarvisCyan,
                    contentColor = JarvisNavySurface
                ),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("send_command_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Transmit Command",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Hardware Controls Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Unmute
            IconButton(
                onClick = onToggleSpokenResponses,
                modifier = Modifier.testTag("toggle_voice_mute_button")
            ) {
                Icon(
                    imageVector = if (spokenResponsesEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = "Toggle Spoken Voice",
                    tint = if (spokenResponsesEnabled) JarvisCyan else JarvisTextSecondary
                )
            }

            // Stop Speaking / Interrupt button
            if (isSpeaking) {
                IconButton(
                    onClick = onStopSpeaking,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = JarvisRed.copy(alpha = 0.2f)),
                    modifier = Modifier.testTag("interrupt_speech_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Interrupt Transmission",
                        tint = JarvisRed
                    )
                }
            } else {
                IconButton(
                    onClick = onOpenCapabilities,
                    modifier = Modifier.testTag("open_capabilities_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Device Subsystems",
                        tint = JarvisCyanLight
                    )
                }
            }

            // Central Main Mic Listening Button
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(if (isListening) JarvisCyan.copy(alpha = 0.25f) else JarvisNavyElevated)
                    .border(2.dp, if (isListening) JarvisCyan else JarvisCyanDark, CircleShape)
                    .scale(if (isListening) micScale else 1f),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (isListening) onStopListening() else onStartListening()
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("mic_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                        contentDescription = "Activate Vocal Receiver",
                        tint = if (isListening) Color.White else JarvisCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Stop / Cancel button
            IconButton(
                onClick = {
                    if (isSpeaking) onStopSpeaking()
                    if (isListening) onStopListening()
                },
                modifier = Modifier.testTag("stop_action_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Halt Vocal System",
                    tint = if (isListening || isSpeaking) JarvisRed else JarvisTextSecondary
                )
            }

            // Settings button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("open_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "System Configuration",
                    tint = JarvisCyanLight
                )
            }
        }
    }
}
