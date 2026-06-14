package com.tensei.upscale.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val AppColorScheme = darkColorScheme(
    primary = LightAccent,
    secondary = GlowAccent,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = White,
    onSecondary = DarkBackground,
    onBackground = HighContrastText,
    onSurface = HighContrastText,
    onSurfaceVariant = MediumContrastText
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = AppColorScheme, typography = Typography, content = content)
}

