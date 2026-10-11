package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantState
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisIceBlue
import com.example.ui.theme.JarvisRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MobileJarvisOrb(
    state: AssistantState,
    audioAmplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MobileOrbTransition")

    // Breathing pulse
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathScale"
    )

    // Inner ring rotation
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.THINKING -> 2000
                    AssistantState.EXECUTING -> 1500
                    AssistantState.LISTENING -> 4000
                    else -> 10000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationInner"
    )

    // Outer ring counter-rotation
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.THINKING -> 3000
                    AssistantState.EXECUTING -> 2500
                    AssistantState.LISTENING -> 6000
                    else -> 16000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationOuter"
    )

    // Glow pulse for speaking/listening
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowAlpha"
    )

    val baseColor = when (state) {
        AssistantState.ERROR -> JarvisRed
        AssistantState.EXECUTING -> JarvisGold
        AssistantState.THINKING -> JarvisIceBlue
        AssistantState.LISTENING -> JarvisCyanLight
        AssistantState.SPEAKING -> JarvisCyan
        AssistantState.IDLE -> JarvisCyanDark
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2f * 0.85f

            // Dynamic reactive amplification
            val ampBoost = if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
                (audioAmplitude * 0.25f)
            } else 0f

            val currentRadius = baseRadius * breathScale * (1f + ampBoost)

            // 1. Outermost Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        baseColor.copy(alpha = if (state == AssistantState.SPEAKING || state == AssistantState.LISTENING) glowAlpha * 0.6f else 0.25f),
                        baseColor.copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 1.3f
                ),
                radius = currentRadius * 1.3f,
                center = center
            )

            // 2. Concentric Outer Ring with dashes
            drawCircle(
                color = baseColor.copy(alpha = 0.45f),
                radius = currentRadius * 0.95f,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), rotationOuter)
                )
            )

            // 3. Middle Thin Ring
            drawCircle(
                color = baseColor.copy(alpha = 0.65f),
                radius = currentRadius * 0.72f,
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 12f), rotationInner)
                )
            )

            // 4. Crosshair Tick Marks (4 axis pointers)
            val tickLen = 10.dp.toPx()
            val tickRadius = currentRadius * 0.72f
            for (i in 0 until 4) {
                val angleRad = Math.toRadians((rotationInner + (i * 90)).toDouble())
                val startX = center.x + (tickRadius - tickLen / 2) * cos(angleRad).toFloat()
                val startY = center.y + (tickRadius - tickLen / 2) * sin(angleRad).toFloat()
                val endX = center.x + (tickRadius + tickLen / 2) * cos(angleRad).toFloat()
                val endY = center.y + (tickRadius + tickLen / 2) * sin(angleRad).toFloat()
                drawLine(
                    color = JarvisCyanLight.copy(alpha = 0.8f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 5. Inner Core Halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.9f),
                        baseColor.copy(alpha = 0.85f),
                        baseColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * 0.45f
                ),
                radius = currentRadius * 0.45f,
                center = center
            )

            // 6. Radiant Center Arc Core
            drawCircle(
                color = Color.White,
                radius = currentRadius * 0.18f,
                center = center
            )
        }
    }
}
