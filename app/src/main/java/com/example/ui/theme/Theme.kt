package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = JarvisBlack,
    primaryContainer = JarvisNavyElevated,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisBlueAccent,
    onSecondary = JarvisBlack,
    secondaryContainer = JarvisNavySurface,
    onSecondaryContainer = JarvisIceBlue,
    tertiary = JarvisGold,
    onTertiary = JarvisBlack,
    background = JarvisBlack,
    onBackground = JarvisTextPrimary,
    surface = JarvisNavySurface,
    onSurface = JarvisTextPrimary,
    surfaceVariant = JarvisDeepNavy,
    onSurfaceVariant = JarvisTextSecondary,
    outline = JarvisGlassBorder,
    error = JarvisRed,
    onError = Color.White
)

@Composable
fun JarvisTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias for template
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    JarvisTheme(content = content)
}
