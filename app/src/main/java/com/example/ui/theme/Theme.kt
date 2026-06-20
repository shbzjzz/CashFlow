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

@Composable
private fun buildDarkColorScheme(theme: ColorTheme) = darkColorScheme(
    primary = theme.dark,
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF312E81),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = theme.secondary,
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    background = BackgroundDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = SecondaryTextDark,
    outlineVariant = Color(0xFF3F3F5A),
    error = ErrorRed
)

@Composable
private fun buildLightColorScheme(theme: ColorTheme) = lightColorScheme(
    primary = theme.light,
    onPrimary = Color.White,
    primaryContainer = theme.container,
    onPrimaryContainer = Color(0xFF312E81),
    secondary = theme.secondary,
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    background = BackgroundLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = Color(0xFFF0F0FF),
    onSurfaceVariant = SecondaryTextLight,
    outlineVariant = Color(0xFFE2E2F0),
    error = ErrorRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorTheme: ColorTheme = defaultColorTheme,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> buildDarkColorScheme(colorTheme)
        else -> buildLightColorScheme(colorTheme)
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
