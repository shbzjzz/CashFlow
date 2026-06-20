package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ─────────────────────────────────────────────────────────────
//  Mint Finance — Color Schemes
//  Light: warm cream surfaces, sage primary, deep forest text
//  Dark : deep green-black surfaces, light mint text, sage accents
// ─────────────────────────────────────────────────────────────

private val LightColorScheme = lightColorScheme(
    // Brand
    primary            = MintPrimary,
    onPrimary          = Color.White,
    primaryContainer   = MintSoft,
    onPrimaryContainer = MintDeep,
    inversePrimary     = MintDeep,

    // Secondary (warm sand)
    secondary          = SandSecondary,
    onSecondary        = Color.White,
    secondaryContainer = SandSoft,
    onSecondaryContainer = SandDeep,

    // Tertiary (soft leaf)
    tertiary           = SuccessLeaf,
    onTertiary         = Color.White,
    tertiaryContainer  = Color(0xFFDDEEDF),
    onTertiaryContainer = Color(0xFF1F4A30),

    // Surfaces
    background         = CreamBackground,
    onBackground       = TextPrimaryLight,
    surface            = CreamSurface,
    onSurface          = TextPrimaryLight,
    surfaceVariant     = CreamSurfaceAlt,
    onSurfaceVariant   = TextSecondaryLight,
    surfaceTint        = MintPrimary,
    inverseSurface     = TextPrimaryLight,
    inverseOnSurface   = CreamBackground,

    // Outlines & dividers
    outline            = TextSecondaryLight,
    outlineVariant     = DividerLight,

    // Functional
    error              = ErrorCoral,
    onError            = Color.White,
    errorContainer     = Color(0xFFFBE2DD),
    onErrorContainer   = Color(0xFF7A2F25),

    // Fix colors for older Material defaults
    scrim              = Color(0x99000000),
)

private val DarkColorScheme = darkColorScheme(
    // Brand
    primary            = MintPrimaryDark,
    onPrimary          = Color(0xFF0E1F1A),
    primaryContainer   = MintDeep,
    onPrimaryContainer = MintSoft,
    inversePrimary     = MintPrimary,

    // Secondary (warm sand, dimmed)
    secondary          = SandSecondary,
    onSecondary        = Color(0xFF2A1F12),
    secondaryContainer = Color(0xFF4A3722),
    onSecondaryContainer = SandSoft,

    // Tertiary
    tertiary           = SuccessLeaf,
    onTertiary         = Color(0xFF0E2418),
    tertiaryContainer  = Color(0xFF1F4A30),
    onTertiaryContainer = Color(0xFFDDEEDF),

    // Surfaces
    background         = DarkBackground,
    onBackground       = TextPrimaryDark,
    surface            = DarkSurface,
    onSurface          = TextPrimaryDark,
    surfaceVariant     = DarkSurfaceAlt,
    onSurfaceVariant   = TextSecondaryDark,
    surfaceTint        = MintPrimaryDark,
    inverseSurface     = Color(0xFFE8F0EB),
    inverseOnSurface   = Color(0xFF121A17),

    // Outlines & dividers
    outline            = TextSecondaryDark,
    outlineVariant     = DividerDark,

    // Functional
    error              = ErrorCoral,
    onError            = Color(0xFF2B0F0A),
    errorContainer     = Color(0xFF7A2F25),
    onErrorContainer   = Color(0xFFFBE2DD),

    scrim              = Color(0xCC000000),
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color by default so the Mint Finance brand identity is preserved
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
