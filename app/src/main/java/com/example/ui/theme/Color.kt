package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// PRIMARY COLOR THEMES (Switchable)
// ─────────────────────────────────────────────────────────────
enum class ColorTheme(
    val light: Color,
    val dark: Color,
    val secondary: Color,
    val container: Color,
    val label: String
) {
    INDIGO(
        light = Color(0xFF6366F1),
        dark = Color(0xFF818CF8),
        secondary = Color(0xFF8B5CF6),
        container = Color(0xFFEEF2FF),
        label = "Indigo"
    ),
    BLUE(
        light = Color(0xFF3B82F6),
        dark = Color(0xFF60A5FA),
        secondary = Color(0xFF0EA5E9),
        container = Color(0xFFEFF6FF),
        label = "Blue"
    ),
    PURPLE(
        light = Color(0xFFA855F7),
        dark = Color(0xFFD8B4FE),
        secondary = Color(0xFF7C3AED),
        container = Color(0xFFFAF5FF),
        label = "Purple"
    ),
    TEAL(
        light = Color(0xFF14B8A6),
        dark = Color(0xFF2DD4BF),
        secondary = Color(0xFF0D9488),
        container = Color(0xFFF0FDFA),
        label = "Teal"
    ),
    EMERALD(
        light = Color(0xFF10B981),
        dark = Color(0xFF6EE7B7),
        secondary = Color(0xFF059669),
        container = Color(0xFFF0FDF4),
        label = "Emerald"
    ),
    ROSE(
        light = Color(0xFFF43F5E),
        dark = Color(0xFFFB7185),
        secondary = Color(0xFFE11D48),
        container = Color(0xFFFFF1F5),
        label = "Rose"
    )
}

// Default theme
val defaultColorTheme = ColorTheme.INDIGO

// ── Light Theme Base ─────────────────────────────────────────────
val BackgroundLight     = Color(0xFFF8F9FF) // Slightly tinted off-white
val SurfaceLight        = Color(0xFFFFFFFF) // Pure white cards
val TextLight           = Color(0xFF1E1B4B) // Deep readable text
val SecondaryTextLight  = Color(0xFF6B7280) // Gray-500

// ── Dark Theme Base ──────────────────────────────────────────────
val BackgroundDark      = Color(0xFF0F0F1A) // Near-black
val SurfaceDark         = Color(0xFF1C1C2E) // Elevated dark surface
val SurfaceDarkElevated = Color(0xFF252535) // Higher elevation for distinction
val TextDark            = Color(0xFFE0E7FF) // Warm white text
val SecondaryTextDark   = Color(0xFF9CA3AF) // Gray-400

// ── Functional Colors ───────────────────────────────────────────
val SuccessGreen        = Color(0xFF10B981) // Emerald-500
val ErrorRed            = Color(0xFFEF4444) // Red-500
val WarningOrange       = Color(0xFFF59E0B) // Amber-500
val InfoBlue            = Color(0xFF3B82F6) // Blue-500
