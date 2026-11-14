package com.flux.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * "Selva nocturna": deep greens (primary), vivid lime (secondary) and orchid (tertiary) accents
 * over moss neutrals. Contrast of every text pair in DECISIONS.md and FluxPaletteContrastTest.
 */
val NightJungle = FluxPalette(
    light = FluxRoles(
        primary = Color(0xFF1B6B44), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFA6F2C2), onPrimaryContainer = Color(0xFF002111),
        secondary = Color(0xFF4A6600), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCDEE7F), onSecondaryContainer = Color(0xFF141F00),
        tertiary = Color(0xFFA23A72), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFD8EA), onTertiaryContainer = Color(0xFF3D0026),
        background = Color(0xFFF5FBF5), surface = Color(0xFFFBFDF8), onSurface = Color(0xFF171D19),
        surfaceVariant = Color(0xFFDCE5DC), onSurfaceVariant = Color(0xFF404943),
        containerLow = Color(0xFFEFF5EF), container = Color(0xFFE9F0E9), containerHigh = Color(0xFFE3EAE3),
        outline = Color(0xFF707971), outlineVariant = Color(0xFFC0C9C0),
        error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    ),
    dark = FluxRoles(
        primary = Color(0xFF6EE7A0), onPrimary = Color(0xFF00391F),
        primaryContainer = Color(0xFF005232), onPrimaryContainer = Color(0xFFA6F2C2),
        secondary = Color(0xFFB1D266), onSecondary = Color(0xFF253600),
        secondaryContainer = Color(0xFF384E00), onSecondaryContainer = Color(0xFFCDEE7F),
        tertiary = Color(0xFFFFAED7), onTertiary = Color(0xFF5E1143),
        tertiaryContainer = Color(0xFF842B5A), onTertiaryContainer = Color(0xFFFFD8EA),
        background = Color(0xFF0B1410), surface = Color(0xFF0F1A14), onSurface = Color(0xFFDDE5DE),
        surfaceVariant = Color(0xFF2A3A31), onSurfaceVariant = Color(0xFFBFC9C0),
        containerLow = Color(0xFF131F18), container = Color(0xFF18261E), containerHigh = Color(0xFF223128),
        outline = Color(0xFF8A938B), outlineVariant = Color(0xFF2A3A31),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    ),
)
