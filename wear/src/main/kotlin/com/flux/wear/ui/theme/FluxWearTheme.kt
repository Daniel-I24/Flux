package com.flux.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.flux.core.designsystem.theme.FluxColorTokens
import com.flux.core.designsystem.theme.FluxRoles
import com.flux.core.designsystem.theme.roles
import com.flux.core.model.ThemePalette

/** Wear OS is always dark: the palette's dark variant on true black, for OLED and glanceability. */
private fun FluxRoles.toWearScheme() = ColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = secondary,
    onSecondary = onSecondary,
    secondaryContainer = secondaryContainer,
    onSecondaryContainer = onSecondaryContainer,
    tertiary = tertiary,
    onTertiary = onTertiary,
    tertiaryContainer = tertiaryContainer,
    onTertiaryContainer = onTertiaryContainer,
    background = FluxColorTokens.Black,
    onBackground = onSurface,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    surfaceContainerLow = containerLow,
    surfaceContainer = container,
    surfaceContainerHigh = containerHigh,
    outline = outline,
    error = error,
    onError = onError,
)

/** "Alto contraste" setting: pure white accents and text on the same palette. */
private fun ColorScheme.highContrast() = copy(
    primary = FluxColorTokens.White,
    onPrimary = FluxColorTokens.Black,
    primaryContainer = FluxColorTokens.White,
    onPrimaryContainer = FluxColorTokens.Black,
    onSurfaceVariant = FluxColorTokens.White,
)

@Composable
fun FluxWearTheme(
    palette: ThemePalette = ThemePalette.ANDEAN_SUNSET,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = palette.roles().dark.toWearScheme()
    MaterialTheme(colorScheme = if (highContrast) scheme.highContrast() else scheme, content = content)
}
