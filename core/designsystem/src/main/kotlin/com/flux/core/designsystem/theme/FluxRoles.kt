package com.flux.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.flux.core.model.ThemePalette

/** The color roles a FLUX theme defines for one variant (light or dark). Screens read Material roles only. */
data class FluxRoles(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val containerLow: Color,
    val container: Color,
    val containerHigh: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
)

/** Light and dark variants of one palette. */
data class FluxPalette(val light: FluxRoles, val dark: FluxRoles)

fun ThemePalette.roles(): FluxPalette = when (this) {
    ThemePalette.ANDEAN_SUNSET -> AndeanSunset
    ThemePalette.NIGHT_JUNGLE -> NightJungle
}

fun FluxRoles.toColorScheme(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary, onPrimary = onPrimary, primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
        secondary = secondary, onSecondary = onSecondary,
        secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary, onTertiary = onTertiary, tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
        background = background, onBackground = onSurface, surface = surface, onSurface = onSurface,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
        surfaceContainerLowest = background, surfaceContainerLow = containerLow, surfaceContainer = container,
        surfaceContainerHigh = containerHigh, surfaceContainerHighest = containerHigh,
        outline = outline, outlineVariant = outlineVariant, error = error, onError = onError,
        inverseSurface = onSurface, inverseOnSurface = surface, inversePrimary = if (dark) primaryContainer else primaryContainer,
        surfaceTint = primary,
    )
}
