package com.example.tempestia.utils

import androidx.compose.ui.graphics.Color

data class TempestiaColors(
    val bgDeep: Color,
    val bgSurface: Color,
    val bgCard: Color,
    val glass: Color,
    val glassBorder: Color,
    val purpleCore: Color,
    val purpleBright: Color,
    val purpleSoft: Color,
    val goldLight: Color,
    val text1: Color,
    val text2: Color,
    val text3: Color,
    val isDark: Boolean
)

val LightTempestiaColors = TempestiaColors(
    bgDeep = Color(0xFFF0ECE8),
    bgSurface = Color(0xFFDDD5F0),
    bgCard = Color(0xD9FFFFFF),
    glass = Color(0x0F7C3AED),
    glassBorder = Color(0x2E7C3AED),
    purpleCore = Color(0xFF7C3AED),
    purpleBright = Color(0xFF9333EA),
    purpleSoft = Color(0xFF7C3AED),
    goldLight = Color(0xFFF59E0B),
    text1 = Color(0xFF1E1040),
    text2 = Color(0xFF4C1D95),
    text3 = Color(0xFF6D5A9E),
    isDark = false
)

val DarkTempestiaColors = TempestiaColors(
    bgDeep = Color(0xFF0d0a1a),
    bgSurface = Color(0xFF231848),
    bgCard = Color(0xD91D113C),
    glass = Color(0x147C3AED),
    glassBorder = Color(0x38A855F7),
    purpleCore = Color(0xFF7C3AED),
    purpleBright = Color(0xFFA855F7),
    purpleSoft = Color(0xFFC4B5FD),
    goldLight = Color(0xFFFCD34D),
    text1 = Color(0xFFF3F0FF),
    text2 = Color(0xFFE9D5FF),
    text3 = Color(0xFF8B7EC8),
    isDark = true
)
