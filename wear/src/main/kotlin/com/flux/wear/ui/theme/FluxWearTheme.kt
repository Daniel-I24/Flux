package com.flux.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.flux.core.designsystem.theme.FluxColorTokens as T

/** Wear OS is always dark: the "Aurora" dark tokens on true black for OLED and glanceability. */
private val FluxWearColorScheme = ColorScheme(
    primary = T.Violet80,
    onPrimary = T.Violet20,
    primaryContainer = T.Violet30,
    onPrimaryContainer = T.Violet90,
    secondary = T.Lagoon80,
    onSecondary = T.Lagoon20,
    secondaryContainer = T.Lagoon30,
    onSecondaryContainer = T.Lagoon90,
    tertiary = T.Ember80,
    onTertiary = T.Ember20,
    tertiaryContainer = T.Ember30,
    onTertiaryContainer = T.Ember90,
    background = T.Ink0,
    onBackground = T.Ink90,
    onSurface = T.Ink90,
    onSurfaceVariant = T.Ink80,
    surfaceContainerLow = T.Ink10,
    surfaceContainer = T.Ink15,
    surfaceContainerHigh = T.Ink20,
    outline = T.Ink60,
    error = T.Red80,
    onError = T.Red20,
)

/** "Alto contraste" setting: brighter accents and pure white text. */
private val FluxWearHighContrastScheme = FluxWearColorScheme.copy(
    primary = T.Ink100,
    onPrimary = T.Ink0,
    primaryContainer = T.Ink100,
    onPrimaryContainer = T.Ink0,
    onSurfaceVariant = T.Ink100,
    surfaceContainer = T.Ink20,
)

@Composable
fun FluxWearTheme(highContrast: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (highContrast) FluxWearHighContrastScheme else FluxWearColorScheme, content = content)
}
