package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CarbonDarkColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Color(0xFF381E72), // Elegant deep purple from design template
    secondary = TechCyan,
    onSecondary = Color(0xFF332D41),
    tertiary = ElectricOrange,
    background = Color(0xFF131215),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF1D1B22),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF26242C),
    onSurfaceVariant = Color(0xFFE6E1E5),
    outline = Color(0xFF49454F)
)

private val CarbonLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFF3F2F7),
    onBackground = Color(0xFF131215),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF131215),
    surfaceVariant = Color(0xFFEAEAEE),
    onSurfaceVariant = Color(0xFF131215),
    outline = Color(0xFFD4D2DC)
)

// Consistent premium dynamic theme solver
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default if unmodified
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Sync the dynamic ThemeConfig so imported references to colors adapt automatically!
    ThemeConfig.isDarkTheme = darkTheme

    val colors = if (darkTheme) {
        CarbonDarkColorScheme
    } else {
        CarbonLightColorScheme
    }

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
