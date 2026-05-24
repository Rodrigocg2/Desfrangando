package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

// Dynamic global theme state manager for the high-performance gym aesthetic
object ThemeConfig {
    var isDarkTheme by mutableStateOf(true)
}

val CarbonBg: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF131215) else Color(0xFFF3F2F7)

val CarbonSurface: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF1D1B22) else Color(0xFFFFFFFF)

val CarbonCard: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF26242C) else Color(0xFFEAEAEE)

val ToxicGreen: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFD0BCFF) else Color(0xFF6750A4) // Dynamic Brand Violet/Purple Accents

val ToxicGreenMuted: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF4A4458) else Color(0xFFE8DDFF)

val ElectricOrange: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFEFB8C8) else Color(0xFF7D5260) // Secondary accent

val TechCyan: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFCCC2DC) else Color(0xFF625B71) // Secondary gray-purple

val TextPrimary: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFE6E1E5) else Color(0xFF131215)

val TextMuted: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFFCAC4D0) else Color(0xFF5F5D66)

val BorderDark: Color
    get() = if (ThemeConfig.isDarkTheme) Color(0xFF49454F) else Color(0xFFD4D2DC)

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6750A4)
val PurpleGrey40 = Color(0xFF625B71)
val Pink40 = Color(0xFF7D5260)
