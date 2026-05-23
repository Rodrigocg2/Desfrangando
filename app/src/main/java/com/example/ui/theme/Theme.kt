package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CarbonDarkColorScheme = darkColorScheme(
    primary = ToxicGreen,
    onPrimary = Color(0xFF381E72), // Elegant deep purple from design template
    secondary = TechCyan,
    onSecondary = Color(0xFF332D41),
    tertiary = ElectricOrange,
    background = CarbonBg,
    onBackground = TextPrimary,
    surface = CarbonSurface,
    onSurface = TextPrimary,
    surfaceVariant = CarbonCard,
    onSurfaceVariant = TextPrimary,
    outline = BorderDark
)

// Consistent premium dark theme for maximum focus
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for elite bodybuilding focus
    dynamicColor: Boolean = false, // Disable to preserve custom carbon-brand identity
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CarbonDarkColorScheme,
        typography = Typography,
        content = content
    )
}
