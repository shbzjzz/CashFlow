package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
 redesign/mint-finance-skin
//  Mint Finance — Color System
//  Soft sage/mint primary, warm cream surfaces, gentle accents.
//  Designed for a calm, premium personal-finance feel.
// ─────────────────────────────────────────────────────────────

// ── Primary Mint / Sage Palette ─────────────────────────────
val MintPrimary        = Color(0xFF4F9D8A) // Sage-500 — calm brand color
val MintPrimaryDark    = Color(0xFF7DBBA8) // Sage-300 — lighter for dark mode
val MintDeep           = Color(0xFF2F6B5C) // Sage-700 — depth + emphasis
val MintSoft           = Color(0xFFE6F2EE) // Sage-50  — soft tinted surface
val MintHover          = Color(0xFF3C8775) // Sage-600 — pressed / hover state

// ── Secondary — Warm Sand ───────────────────────────────────
val SandSecondary      = Color(0xFFD4A373) // Sand-400 — warm complement
val SandDeep           = Color(0xFFA87B4D) // Sand-600 — readable on cream
val SandSoft           = Color(0xFFF5EBDD) // Sand-50  — chip / hover surface

// ── Light Theme Surfaces ────────────────────────────────────
val CreamBackground    = Color(0xFFFBF8F1) // Warm off-white cream
val CreamSurface       = Color(0xFFFFFFFF) // Pure white cards
val CreamSurfaceAlt    = Color(0xFFF6F1E7) // Slightly deeper cream for alt cards
val TextPrimaryLight   = Color(0xFF22372E) // Deep forest — high contrast on cream
val TextSecondaryLight = Color(0xFF6B7F75) // Muted sage-gray
val DividerLight        = Color(0xFFE2DFD4) // Warm hairline divider

// ── Dark Theme Surfaces ─────────────────────────────────────
val DarkBackground     = Color(0xFF121A17) // Near-black with green undertone
val DarkSurface        = Color(0xFF1C2823) // Elevated surface
val DarkSurfaceAlt     = Color(0xFF24332C) // Higher elevation
val TextPrimaryDark    = Color(0xFFE8F0EB) // Warm mint-tinted white
val TextSecondaryDark  = Color(0xFF9FB2A8) // Muted sage-gray
val DividerDark        = Color(0xFF324339) // Subtle elevated divider

// ── Functional / Semantic Colors ────────────────────────────
val SuccessLeaf        = Color(0xFF5BB98C) // Soft success green
val ErrorCoral         = Color(0xFFE07A6E) // Warm coral error
val WarningAmber       = Color(0xFFE2A93B) // Muted amber warning
val InfoSky            = Color(0xFF6BA4C7) // Calm info blue

// ── Accent / Category Tints ─────────────────────────────────
val AccentLavender     = Color(0xFF9C8EC4) // Soft category accent
val AccentPeach        = Color(0xFFE8A07E) // Warm category accent
val AccentRose         = Color(0xFFD88B9C) // Soft rose accent
val AccentOlive        = Color(0xFF8FA362) // Earthy olive

// ── Glow / Overlay Tints ────────────────────────────────────
val MintGlow           = Color(0x264F9D8A) // Mint at 15% — subtle brand glow
val CreamOverlay       = Color(0x40FFFFFF) // 25% white overlay for glass effect
val DarkOverlay        = Color(0x66000000) // 40% black overlay for dark scrims

// ── Backward-compatibility aliases ──────────────────────────
// Older Screens.kt references these names. They map to the closest Mint Finance
// equivalents so the rest of the app keeps compiling without touching call sites.
val SuccessGreen       = SuccessLeaf
val CashFlowPrimary    = MintPrimary
val CashFlowPrimaryDark = MintPrimaryDark
val CashFlowSecondary  = SandSecondary
val CashFlowLightIndigo = MintSoft
val BackgroundLight    = CreamBackground
val SurfaceLight       = CreamSurface
val TextLight          = TextPrimaryLight
val SecondaryTextLight = TextSecondaryLight
val BackgroundDark     = DarkBackground
val SurfaceDark        = DarkSurface
val TextDark           = TextPrimaryDark
val SecondaryTextDark  = TextSecondaryDark
val ErrorRed           = ErrorCoral
val WarningOrange      = WarningAmber
val IndigoGlow         = MintGlow

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
 main
