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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InterfaceMode
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
fun SettingsScreen(
    viewModel: JarvisViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val geminiTestResult by viewModel.geminiTestResult.collectAsState()

    var geminiKeyInput by remember(settings.geminiApiKey) { mutableStateOf(settings.geminiApiKey) }
    var geminiKeyVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JarvisBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("jarvis_settings_screen")
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
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = JarvisCyan
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "SYSTEM CONFIGURATION",
                        style = MaterialTheme.typography.headlineMedium,
                        color = JarvisCyan
                    )
                    Text(
                        text = "J.A.R.V.I.S. Architecture & Telemetry Dashboard",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextSecondary
                    )
                }
            }

            HorizontalDivider(color = JarvisGlassBorder)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==========================================
                // 1. DISPLAY & INTERFACE
                // ==========================================
                SettingsSectionCard(title = "DISPLAY & INTERFACE") {
                    Text(
                        text = "Interface Mode Selection",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanLight
                    )
                    Text(
                        text = "Select whether to adapt automatically to display size or enforce a specific terminal mode.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    InterfaceModeOption(
                        title = "Automatic (Adaptive Display)",
                        subtitle = "Selects phone or tablet UI dynamically based on screen geometry.",
                        selected = settings.interfaceMode == InterfaceMode.AUTOMATIC,
                        onSelect = { viewModel.setInterfaceMode(InterfaceMode.AUTOMATIC) },
                        testTag = "mode_automatic"
                    )

                    InterfaceModeOption(
                        title = "Mobile Interface",
                        subtitle = "Forces compact smartphone layout with cyan arc core and single-column activity feed.",
                        selected = settings.interfaceMode == InterfaceMode.MOBILE,
                        onSelect = { viewModel.setInterfaceMode(InterfaceMode.MOBILE) },
                        testTag = "mode_mobile"
                    )

                    InterfaceModeOption(
                        title = "Tablet Interface",
                        subtitle = "Forces command center layout with 3-pane holographic HUD and multi-ring orbital orb.",
                        selected = settings.interfaceMode == InterfaceMode.TABLET,
                        onSelect = { viewModel.setInterfaceMode(InterfaceMode.TABLET) },
                        testTag = "mode_tablet"
                    )
                }

                // ==========================================
                // 2. AI ENGINE SETTINGS (GEMINI)
                // ==========================================
                SettingsSectionCard(title = "AI ENGINE SETTINGS (GEMINI)") {
                    Text(
                        text = "Gemini API Key",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanLight
                    )
                    Text(
                        text = "Enter your Google Gemini API key to activate natural language reasoning and tool execution.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = geminiKeyInput,
                        onValueChange = { geminiKeyInput = it },
                        placeholder = { Text("Enter Gemini API Key...", color = JarvisTextSecondary) },
                        visualTransformation = if (geminiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { geminiKeyVisible = !geminiKeyVisible }) {
                                Icon(
                                    imageVector = if (geminiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility",
                                    tint = JarvisCyan
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JarvisCyan,
                            unfocusedBorderColor = JarvisGlassBorder,
                            focusedTextColor = JarvisTextPrimary,
                            unfocusedTextColor = JarvisTextPrimary,
                            focusedContainerColor = JarvisNavyElevated,
                            unfocusedContainerColor = JarvisNavyElevated
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_api_key_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.setGeminiApiKey(geminiKeyInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisNavySurface),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_gemini_key_button")
                        ) {
                            Text("SAVE KEY", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.testGeminiConnection() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisCyanLight),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_gemini_connection_button")
                        ) {
                            Text("TEST LINK", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                geminiKeyInput = ""
                                viewModel.setGeminiApiKey("")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed),
                            modifier = Modifier.testTag("remove_gemini_key_button")
                        ) {
                            Text("CLEAR", fontSize = 11.sp)
                        }
                    }

                    // Test result feedback
                    geminiTestResult?.let { (success, msg) ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (success) JarvisGreen.copy(alpha = 0.15f) else JarvisRed.copy(alpha = 0.15f))
                                .border(1.dp, if (success) JarvisGreen else JarvisRed, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (success) JarvisGreen else JarvisRed,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Model Selection
                    Text(
                        text = "Active Gemini Model",
                        style = MaterialTheme.typography.titleMedium,
                        color = JarvisCyanLight
                    )

                    listOf(
                        "gemini-3.5-flash" to "Fastest multimodal inference & structured actions",
                        "gemini-3.1-pro-preview" to "High-reasoning model for complex queries",
                        "gemini-3.1-flash-lite-preview" to "Ultra-lightweight responsive model"
                    ).forEach { (modelId, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.geminiModel == modelId,
                                onClick = { viewModel.setGeminiModel(modelId) },
                                colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = modelId,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = JarvisTextPrimary
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JarvisTextSecondary
                                )
                            }
                        }
                    }
                }

                // ==========================================
                // 3. NATIVE J.A.R.V.I.S. VOCAL ENGINE
                // ==========================================
                SettingsSectionCard(title = "NATIVE J.A.R.V.I.S. VOCAL ENGINE") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Built-in Neural Speech Engine",
                                style = MaterialTheme.typography.titleMedium,
                                color = JarvisCyanLight
                            )
                            Text(
                                text = "Zero third-party keys required. Uses native Android speech synthesis calibrated with classic British eloquence, resonant pitch, and clear articulation.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JarvisTextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(JarvisGreen.copy(alpha = 0.15f))
                                .border(1.dp, JarvisGreen, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Online",
                                    tint = JarvisGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ONLINE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = JarvisGreen,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Voice Pitch Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vocal Pitch (Resonance)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = JarvisCyanLight
                        )
                        Text(
                            text = "${(settings.voicePitch * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = JarvisGold
                        )
                    }

                    Slider(
                        value = settings.voicePitch,
                        onValueChange = { viewModel.setVoicePitch(it) },
                        valueRange = 0.75f..1.25f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisNavyElevated
                        ),
                        modifier = Modifier.testTag("voice_pitch_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Voice Speed Control
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Speech Rate (Cadence)",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = JarvisCyanLight
                        )
                        Text(
                            text = "${(settings.voiceSpeed * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = JarvisGold
                        )
                    }

                    Slider(
                        value = settings.voiceSpeed,
                        onValueChange = { viewModel.setVoiceSpeed(it) },
                        valueRange = 0.8f..1.3f,
                        steps = 10,
                        colors = SliderDefaults.colors(
                            thumbColor = JarvisCyan,
                            activeTrackColor = JarvisCyan,
                            inactiveTrackColor = JarvisNavyElevated
                        ),
                        modifier = Modifier.testTag("voice_speed_slider")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Test Voice Button
                    Button(
                        onClick = { viewModel.testVoicePlayback() },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisNavySurface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_jarvis_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TEST J.A.R.V.I.S. VOICE SYNTHESIS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // ==========================================
                // 4. ASSISTANT BEHAVIOR & CONTROLS
                // ==========================================
                SettingsSectionCard(title = "ASSISTANT BEHAVIOR") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Spoken Audio Responses",
                                style = MaterialTheme.typography.titleMedium,
                                color = JarvisCyanLight
                            )
                            Text(
                                text = "Audibly pronounce responses and system status.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JarvisTextSecondary
                            )
                        }
                        Switch(
                            checked = settings.spokenResponsesEnabled,
                            onCheckedChange = { viewModel.setSpokenResponses(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisNavyElevated),
                            modifier = Modifier.testTag("toggle_spoken_responses_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Require Confirmation Before Action",
                                style = MaterialTheme.typography.titleMedium,
                                color = JarvisCyanLight
                            )
                            Text(
                                text = "Prompts operator with confirmation banner before executing device commands.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JarvisTextSecondary
                            )
                        }
                        Switch(
                            checked = settings.requireConfirmationBeforeAction,
                            onCheckedChange = { viewModel.setRequireConfirmation(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = JarvisCyan, checkedTrackColor = JarvisNavyElevated),
                            modifier = Modifier.testTag("toggle_confirmation_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { viewModel.clearConversation() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("clear_history_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("CLEAR CONVERSATION HISTORY", fontSize = 11.sp)
                    }
                }

                // ==========================================
                // 5. PRIVACY & SECURITY
                // ==========================================
                SettingsSectionCard(title = "PRIVACY & SECURITY") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = JarvisGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CREDENTIAL ISOLATION",
                            style = MaterialTheme.typography.titleMedium,
                            color = JarvisGold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "API credentials entered into J.A.R.V.I.S. are stored locally inside the application's private preferences. They are never transmitted to third parties except directly to Google Gemini during conversational inference.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            geminiKeyInput = ""
                            viewModel.clearCredentials()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("wipe_credentials_button")
                    ) {
                        Text("WIPE SAVED CREDENTIALS FROM TERMINAL", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = JarvisNavySurface.copy(alpha = 0.9f)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, JarvisGlassBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = JarvisCyan
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = JarvisGlassBorder
            )
            content()
        }
    }
}

@Composable
fun InterfaceModeOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onSelect: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) JarvisNavyElevated else Color.Transparent)
            .border(1.dp, if (selected) JarvisCyanDark else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(selectedColor = JarvisCyan)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (selected) JarvisCyan else JarvisTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = JarvisTextSecondary
            )
        }
    }
}
