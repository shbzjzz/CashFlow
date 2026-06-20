package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ─────────────────────────────────────────────────────────────
// SPACING SCALE
// ─────────────────────────────────────────────────────────────
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val giant = 40.dp

    // Common composites
    val screenPadding = lg
    val cardPadding = xxl
    val itemSpacing = md
    val sectionSpacing = xl
}

// ─────────────────────────────────────────────────────────────
// CORNER RADIUS TOKENS
// ─────────────────────────────────────────────────────────────
object Corners {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 28.dp
    val full = 999.dp

    // Shapes for reuse
    val button = RoundedCornerShape(lg)
    val card = RoundedCornerShape(lg)
    val heroCard = RoundedCornerShape(xxl)
    val input = RoundedCornerShape(lg)
    val dialog = RoundedCornerShape(xl)
    val chip = RoundedCornerShape(md)
    val circle = RoundedCornerShape(full)
}

// ─────────────────────────────────────────────────────────────
// ELEVATION & SHADOW TOKENS
// ─────────────────────────────────────────────────────────────
object Shadows {
    object Light {
        val subtle = 2.dp
        val soft = 4.dp
        val medium = 8.dp
        val strong = 12.dp
        val dramatic = 20.dp
    }

    object Dark {
        val subtle = 1.dp
        val soft = 2.dp
        val medium = 6.dp
        val strong = 10.dp
        val dramatic = 16.dp
    }
}

// ─────────────────────────────────────────────────────────────
// STATE COLORS (Hover, Focus, Disabled, etc.)
// ─────────────────────────────────────────────────────────────
object StateColors {
    // Light mode states
    object Light {
        val primaryHover = Color(0xFF5558E3)      // Darker indigo
        val primaryFocus = Color(0xFF4446D4)      // Even darker for focus ring
        val primaryDisabled = Color(0xFFD1D5F7)   // Faded indigo
        val surfaceHover = Color(0xFFF5F5FF)      // Slight indigo tint on hover
        val errorLight = Color(0xFFFEE2E2)        // Error background
        val successLight = Color(0xFFECFDF5)      // Success background
        val warningLight = Color(0xFFFEF3C7)      // Warning background
    }

    // Dark mode states
    object Dark {
        val primaryHover = Color(0xFF7C7FFF)      // Lighter indigo
        val primaryFocus = Color(0xFF9299FF)      // Even lighter
        val primaryDisabled = Color(0xFF2D2D4F)   // Darkened indigo
        val surfaceHover = Color(0xFF252535)      // Lifted dark surface
        val errorLight = Color(0xFF5F2C2C)        // Error background
        val successLight = Color(0xFF2D5A4A)      // Success background
        val warningLight = Color(0xFF5A4A2D)      // Warning background
    }
}

// ─────────────────────────────────────────────────────────────
// ANIMATION DURATIONS
// ─────────────────────────────────────────────────────────────
object AnimationDurations {
    const val instant = 0
    const val fast = 100
    const val normal = 300
    const val slow = 500
    const val verySlow = 800
}

// ─────────────────────────────────────────────────────────────
// SIZE TOKENS (Icon sizes, touch targets, etc.)
// ─────────────────────────────────────────────────────────────
object Sizes {
    // Icon sizes
    val iconXs = 16.dp
    val iconSm = 20.dp
    val iconMd = 24.dp
    val iconLg = 32.dp
    val iconXl = 40.dp
    val iconXxl = 48.dp

    // Touch targets (minimum 48dp)
    val touchMinimum = 48.dp
    val buttonHeight = 52.dp

    // Other
    val appBarHeight = 64.dp
    val bottomNavHeight = 80.dp
    val dividerThickness = 1.dp
}

// ─────────────────────────────────────────────────────────────
// OPACITY TOKENS (Semantic alpha values)
// ─────────────────────────────────────────────────────────────
object Opacity {
    const val disabled = 0.38f
    const val hovered = 0.08f
    const val focused = 0.12f
    const val pressed = 0.16f
    const val dragged = 0.12f
    const val subtle = 0.6f
    const val medium = 0.75f
    const val strong = 1.0f
}

// ─────────────────────────────────────────────────────────────
// COUNTRY-SPECIFIC COLOR BADGES (Dual-country support)
// ─────────────────────────────────────────────────────────────
object CountryColors {
    val UAE = Color(0xFFFF6B35)      // Vibrant orange (UAE flag color)
    val India = Color(0xFF138808)    // Deep green (India flag color)
    val Pakistan = Color(0xFF01411C) // Dark green (Pakistan flag color)
    val Bangladesh = Color(0xFFE10012) // Deep red (Bangladesh flag color)
    val Philippines = Color(0xFF0053BA) // Blue (Philippines flag color)
    val Other = Color(0xFF6B7280)    // Neutral gray

    fun getColorForCountry(countryCode: String): Color = when (countryCode.uppercase()) {
        "UAE", "AE" -> UAE
        "IND", "IN" -> India
        "PAK", "PK" -> Pakistan
        "BGD", "BD" -> Bangladesh
        "PHL", "PH" -> Philippines
        else -> Other
    }
}

// ─────────────────────────────────────────────────────────────
// COMPOSABLE THEME EXTENSIONS (For easy access in composables)
// ─────────────────────────────────────────────────────────────
val spacing = Spacing
val corners = Corners
val shadows = Shadows
val stateColors = StateColors
val animationDurations = AnimationDurations
val sizes = Sizes
val opacity = Opacity
val countryColors = CountryColors
