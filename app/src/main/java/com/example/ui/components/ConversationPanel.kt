package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.DeviceAction
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGlassBorder
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisGreen
import com.example.ui.theme.JarvisIceBlue
import com.example.ui.theme.JarvisNavyElevated
import com.example.ui.theme.JarvisNavySurface
import com.example.ui.theme.JarvisRed
import com.example.ui.theme.JarvisTextPrimary
import com.example.ui.theme.JarvisTextSecondary

@Composable
fun ConversationPanel(
    messages: List<ChatMessage>,
    pendingAction: Pair<DeviceAction, String>?,
    onConfirmAction: () -> Unit,
    onDismissAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = modifier) {
        // Confirmation Banner if pending
        if (pendingAction != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JarvisNavyElevated)
                    .border(1.dp, JarvisGold, RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .testTag("action_confirmation_banner")
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Confirm",
                            tint = JarvisGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AUTHORIZATION REQUIRED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = JarvisGold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pendingAction.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = JarvisTextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onDismissAction,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisRed),
                            modifier = Modifier.testTag("dismiss_action_button")
                        ) {
                            Text("CANCEL", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onConfirmAction,
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan, contentColor = JarvisNavySurface),
                            modifier = Modifier.testTag("confirm_action_button")
                        ) {
                            Text("EXECUTE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bg = if (isUser) JarvisNavyElevated.copy(alpha = 0.8f) else JarvisNavySurface.copy(alpha = 0.9f)
    val borderColor = if (isUser) JarvisCyanDark.copy(alpha = 0.5f) else JarvisGlassBorder.copy(alpha = 0.7f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "OPERATOR" else "J.A.R.V.I.S.",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isUser) JarvisIceBlue else JarvisCyan
                    )

                    if (message.actionSummary != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (message.actionSuccess == true) Icons.Default.CheckCircle else Icons.Default.Close,
                                contentDescription = "Status",
                                tint = if (message.actionSuccess == true) JarvisGreen else JarvisRed,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (message.actionSuccess == true) "COMPLETED" else "FAILED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (message.actionSuccess == true) JarvisGreen else JarvisRed,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = JarvisTextPrimary
                )

                if (message.actionSummary != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisNavyElevated)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "COMMAND: ${message.actionSummary}",
                            style = MaterialTheme.typography.labelSmall,
                            color = JarvisCyanLight,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}
