package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PeekDarkColorScheme = darkColorScheme(
    primary = PeekCyanNeon,
    onPrimary = PeekDeepVoid,
    primaryContainer = PeekElevatedCard,
    onPrimaryContainer = PeekCyanNeon,
    secondary = PeekAmberGold,
    onSecondary = PeekDeepVoid,
    secondaryContainer = Color(0xFF332608),
    onSecondaryContainer = PeekAmberGold,
    tertiary = PeekEmeraldPortal,
    onTertiary = PeekDeepVoid,
    background = PeekDeepVoid,
    onBackground = PeekTextPrimary,
    surface = PeekMidnightSurface,
    onSurface = PeekTextPrimary,
    surfaceVariant = PeekElevatedCard,
    onSurfaceVariant = PeekTextSecondary,
    error = PeekCrimsonHazard,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = PeekDarkColorScheme,
        typography = Typography,
        content = content
    )
}
