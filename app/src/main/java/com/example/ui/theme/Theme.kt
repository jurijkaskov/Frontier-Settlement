package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.ui.motion.FrontierGameMotion

private val DarkColorScheme = darkColorScheme(
    primary = FrontierPrimary,
    onPrimary = FrontierOnPrimary,
    primaryContainer = FrontierPrimaryContainer,
    onPrimaryContainer = FrontierOnPrimaryContainer,
    secondary = FrontierSecondary,
    onSecondary = FrontierOnSecondary,
    secondaryContainer = FrontierSecondaryContainer,
    tertiary = FrontierTertiary,
    onTertiary = FrontierOnTertiary,
    tertiaryContainer = FrontierTertiaryContainer,
    background = FrontierDarkBackground,
    surface = FrontierDarkSurface,
    surfaceVariant = FrontierDarkSurfaceElevated,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextMuted,
    outline = FrontierBorder,
    outlineVariant = FrontierBorderLight
)

@Composable
fun MyApplicationTheme(
    colors: FrontierGameColors = FrontierGameColors(),
    spacing: FrontierGameSpacing = FrontierGameSpacing(),
    shapes: FrontierGameShapes = FrontierGameShapes(),
    typography: FrontierGameTypography = FrontierGameTypography(),
    motion: FrontierGameMotion = FrontierGameMotion(),
    elevation: FrontierGameElevation = FrontierGameElevation(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalFrontierColors provides colors,
        LocalFrontierSpacing provides spacing,
        LocalFrontierShapes provides shapes,
        LocalFrontierTypography provides typography,
        LocalFrontierMotion provides motion,
        LocalFrontierElevation provides elevation
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography,
            content = content
        )
    }
}
