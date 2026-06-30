package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val VaultColorScheme = lightColorScheme(
    primary = HighDensityPrimary,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = HighDensityPrimaryContainer,
    onPrimaryContainer = HighDensityOnPrimaryContainer,
    secondaryContainer = HighDensitySecondaryContainer,
    background = HighDensityBackground,
    onBackground = HighDensityTextPrimary,
    surface = HighDensityBackground,
    onSurface = HighDensityTextPrimary,
    surfaceVariant = HighDensitySurface,
    onSurfaceVariant = HighDensityTextSecondary,
    outline = HighDensityBorder,
    outlineVariant = HighDensityBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Force light theme for High Density
    dynamicColor: Boolean = false, // Disable dynamic color to match design exact hexes
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = VaultColorScheme, typography = Typography, content = content)
}
