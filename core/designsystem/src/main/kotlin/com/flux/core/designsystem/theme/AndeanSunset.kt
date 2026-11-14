package com.flux.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * "Atardecer andino": terracotta (primary), ochre (secondary) and peach (tertiary) over warm
 * clay neutrals. Contrast of every text pair in DECISIONS.md and FluxPaletteContrastTest.
 */
val AndeanSunset = FluxPalette(
    light = FluxRoles(
        primary = Color(0xFFA0442A), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDBCF), onPrimaryContainer = Color(0xFF3A0B00),
        secondary = Color(0xFF7A5900), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFDEA1), onSecondaryContainer = Color(0xFF261900),
        tertiary = Color(0xFF8B4A2F), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDCC7), onTertiaryContainer = Color(0xFF331200),
        background = Color(0xFFFFF8F5), surface = Color(0xFFFFFBFF), onSurface = Color(0xFF231917),
        surfaceVariant = Color(0xFFF5DED6), onSurfaceVariant = Color(0xFF53433F),
        containerLow = Color(0xFFFEF1EC), container = Color(0xFFF8EBE6), containerHigh = Color(0xFFF2E5E0),
        outline = Color(0xFF85736E), outlineVariant = Color(0xFFD8C2BB),
        error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
    ),
    dark = FluxRoles(
        primary = Color(0xFFFFB59E), onPrimary = Color(0xFF561F0F),
        primaryContainer = Color(0xFF7E2E17), onPrimaryContainer = Color(0xFFFFDBCF),
        secondary = Color(0xFFEEC06C), onSecondary = Color(0xFF3F2E00),
        secondaryContainer = Color(0xFF5C4300), onSecondaryContainer = Color(0xFFFFDEA1),
        tertiary = Color(0xFFFFB68C), onTertiary = Color(0xFF522300),
        tertiaryContainer = Color(0xFF713500), onTertiaryContainer = Color(0xFFFFDCC7),
        background = Color(0xFF1A110F), surface = Color(0xFF1F1513), onSurface = Color(0xFFF1DFDA),
        surfaceVariant = Color(0xFF53433F), onSurfaceVariant = Color(0xFFE2CCC5),
        containerLow = Color(0xFF231917), container = Color(0xFF2A1F1C), containerHigh = Color(0xFF352A26),
        outline = Color(0xFFA08C87), outlineVariant = Color(0xFF53433F),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    ),
)
