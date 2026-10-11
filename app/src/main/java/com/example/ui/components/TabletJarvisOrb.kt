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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.AssistantState
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanDark
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.JarvisIceBlue
import com.example.ui.theme.JarvisOrange
import com.example.ui.theme.JarvisRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TabletJarvisOrb(
    state: AssistantState,
    audioAmplitude: Float,
    modifier: Modifier = Modifier,
    size: Dp = 340.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "TabletOrbTransition")

    // Core breath cycle
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CorePulse"
    )

    // Orbital ring 1: clockwise fast
    val rotRing1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.THINKING -> 1800
                    AssistantState.EXECUTING -> 1200
                    AssistantState.LISTENING -> 3500
                    else -> 12000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotRing1"
    )

    // Orbital ring 2: counter-clockwise medium
    val rotRing2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.THINKING -> 2400
                    AssistantState.EXECUTING -> 1800
                    AssistantState.LISTENING -> 4500
                    else -> 18000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotRing2"
    )

    // Segmented ring 3: slow rotation
    val rotRing3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    AssistantState.THINKING -> 3200
                    AssistantState.EXECUTING -> 2200
                    else -> 24000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotRing3"
    )

    // Audio glow shimmer
    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowIntensity"
    )

    val primaryTone = when (state) {
        AssistantState.ERROR -> JarvisRed
        AssistantState.EXECUTING -> JarvisGold
        AssistantState.THINKING -> JarvisOrange
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
            val maxRadius = this.size.minDimension / 2f * 0.92f

            val audioScale = if (state == AssistantState.LISTENING || state == AssistantState.SPEAKING) {
                1f + (audioAmplitude * 0.35f)
            } else 1f

            val dynamicRadius = maxRadius * corePulse * audioScale

            // 1. Massive Holographic Field Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryTone.copy(alpha = if (state == AssistantState.SPEAKING) glowIntensity * 0.7f else 0.3f),
                        primaryTone.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 1.35f
                ),
                radius = dynamicRadius * 1.35f,
                center = center
            )

            // 2. Outermost Tachometer Degree Dial (36 radial calibration marks)
            val dialRadius = dynamicRadius * 0.98f
            for (i in 0 until 36) {
                val angleDeg = (i * 10) + rotRing3
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val isMajor = i % 3 == 0
                val tickLength = if (isMajor) 14.dp.toPx() else 7.dp.toPx()
                val tickWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx()
                val alpha = if (isMajor) 0.8f else 0.4f

                val startX = center.x + (dialRadius - tickLength) * cos(angleRad).toFloat()
                val startY = center.y + (dialRadius - tickLength) * sin(angleRad).toFloat()
                val endX = center.x + dialRadius * cos(angleRad).toFloat()
                val endY = center.y + dialRadius * sin(angleRad).toFloat()

                drawLine(
                    color = primaryTone.copy(alpha = alpha),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = tickWidth,
                    cap = StrokeCap.Round
                )
            }

            // 3. Segmented Holographic Arcs (Layer 1)
            val arcRect = androidx.compose.ui.geometry.Rect(
                center.x - dynamicRadius * 0.82f,
                center.y - dynamicRadius * 0.82f,
                center.x + dynamicRadius * 0.82f,
                center.y + dynamicRadius * 0.82f
            )
            drawArc(
                color = primaryTone.copy(alpha = 0.75f),
                startAngle = rotRing1,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryTone.copy(alpha = 0.75f),
                startAngle = rotRing1 + 180f,
                sweepAngle = 70f,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Counter-rotating Dotted Ring (Layer 2)
            drawCircle(
                color = JarvisIceBlue.copy(alpha = 0.6f),
                radius = dynamicRadius * 0.68f,
                center = center,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 16f), rotRing2)
                )
            )

            // 5. Orbital Satellites / Nodes (3 glowing nodes orbiting the ring)
            val orbitRadius = dynamicRadius * 0.68f
            for (nodeIdx in 0 until 3) {
                val nodeAngleRad = Math.toRadians((rotRing2 + (nodeIdx * 120)).toDouble())
                val nodeX = center.x + orbitRadius * cos(nodeAngleRad).toFloat()
                val nodeY = center.y + orbitRadius * sin(nodeAngleRad).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
                drawCircle(
                    color = primaryTone.copy(alpha = 0.5f),
                    radius = 8.dp.toPx(),
                    center = Offset(nodeX, nodeY)
                )
            }

            // 6. Hexagonal Inner Aegis Ring
            val innerRingRadius = dynamicRadius * 0.52f
            drawCircle(
                color = primaryTone.copy(alpha = 0.85f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 15f), rotRing1)
                )
            )

            // 7. Tactical Crosshairs across the core
            val crossHairLen = dynamicRadius * 0.45f
            drawLine(
                color = primaryTone.copy(alpha = 0.4f),
                start = Offset(center.x - crossHairLen, center.y),
                end = Offset(center.x + crossHairLen, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = primaryTone.copy(alpha = 0.4f),
                start = Offset(center.x, center.y - crossHairLen),
                end = Offset(center.x, center.y + crossHairLen),
                strokeWidth = 1.dp.toPx()
            )

            // 8. Intense Arc Reactor Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        JarvisCyanLight.copy(alpha = 0.95f),
                        primaryTone.copy(alpha = 0.7f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = dynamicRadius * 0.35f
                ),
                radius = dynamicRadius * 0.35f,
                center = center
            )

            // 9. Pure Fusion Center Point
            drawCircle(
                color = Color.White,
                radius = dynamicRadius * 0.14f,
                center = center
            )
        }
    }
}
