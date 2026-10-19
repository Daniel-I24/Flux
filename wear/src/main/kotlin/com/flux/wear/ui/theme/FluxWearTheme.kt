package com.flux.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.flux.core.designsystem.theme.FluxColorTokens as T

/** Wear OS is always dark: tokens tuned for OLED and glanceability. */
private val FluxWearColorScheme = ColorScheme(
    primary = T.Indigo300,
    onPrimary = T.Indigo900,
    primaryContainer = T.Indigo600,
    onPrimaryContainer = T.Indigo50,
    secondary = T.Cyan300,
    onSecondary = T.Cyan900,
    background = T.Black,
    onBackground = T.Slate50,
    onSurface = T.Slate50,
    onSurfaceVariant = T.Slate400,
    surfaceContainer = T.Slate850,
    surfaceContainerHigh = T.Slate800,
    error = T.Red400,
    onError = T.Slate950,
)

/** "Alto contraste" setting: brighter accents and pure white text. */
private val FluxWearHighContrastScheme = FluxWearColorScheme.copy(
    primary = T.White,
    onPrimary = T.Black,
    primaryContainer = T.White,
    onPrimaryContainer = T.Black,
    secondary = T.Cyan300,
    onSurfaceVariant = T.Slate50,
    surfaceContainer = T.Slate800,
)

@Composable
fun FluxWearTheme(highContrast: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (highContrast) FluxWearHighContrastScheme else FluxWearColorScheme, content = content)
}
