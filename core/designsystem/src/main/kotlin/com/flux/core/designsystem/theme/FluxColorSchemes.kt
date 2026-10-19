package com.flux.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import com.flux.core.designsystem.theme.FluxColorTokens as T

val FluxLightColorScheme = lightColorScheme(
    primary = T.Indigo600,
    onPrimary = T.White,
    primaryContainer = T.Indigo50,
    onPrimaryContainer = T.Indigo900,
    secondary = T.Cyan600,
    onSecondary = T.White,
    secondaryContainer = T.Cyan300,
    onSecondaryContainer = T.Cyan900,
    background = T.Slate50,
    onBackground = T.Slate950,
    surface = T.White,
    onSurface = T.Slate950,
    surfaceVariant = T.Slate100,
    onSurfaceVariant = T.Slate600,
    surfaceContainer = T.Slate100,
    surfaceContainerHigh = T.Slate200,
    outline = T.Slate400,
    outlineVariant = T.Slate200,
    error = T.Red700,
    onError = T.White,
)

val FluxDarkColorScheme = darkColorScheme(
    primary = T.Indigo300,
    onPrimary = T.Indigo900,
    primaryContainer = T.Indigo600,
    onPrimaryContainer = T.Indigo50,
    secondary = T.Cyan300,
    onSecondary = T.Cyan900,
    secondaryContainer = T.Cyan900,
    onSecondaryContainer = T.Cyan300,
    background = T.Slate950,
    onBackground = T.Slate50,
    surface = T.Slate900,
    onSurface = T.Slate50,
    surfaceVariant = T.Slate800,
    onSurfaceVariant = T.Slate400,
    surfaceContainer = T.Slate850,
    surfaceContainerHigh = T.Slate800,
    outline = T.Slate600,
    outlineVariant = T.Slate800,
    error = T.Red400,
    onError = T.Slate950,
)
