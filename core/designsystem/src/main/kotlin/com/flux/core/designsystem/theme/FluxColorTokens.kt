package com.flux.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * FLUX "Aurora" palette (v1.1, N-057): violet, lagoon and ember. Every text pair passes WCAG AA
 * (contrast table in DECISIONS.md). Screens never use these directly: they read the theme roles.
 */
object FluxColorTokens {
    // Violet (primary)
    val Violet10 = Color(0xFF1B1260)
    val Violet20 = Color(0xFF24127A)
    val Violet30 = Color(0xFF3B2BB0)
    val Violet40 = Color(0xFF4C3BCF)
    val Violet80 = Color(0xFFC5BBFF)
    val Violet90 = Color(0xFFE6E1FF)

    // Lagoon (secondary)
    val Lagoon10 = Color(0xFF00363B)
    val Lagoon20 = Color(0xFF00373C)
    val Lagoon30 = Color(0xFF004F56)
    val Lagoon40 = Color(0xFF00727D)
    val Lagoon80 = Color(0xFF6ED6DF)
    val Lagoon90 = Color(0xFFC7F0F2)

    // Ember (tertiary: the voice lava and highlights)
    val Ember10 = Color(0xFF3D1000)
    val Ember20 = Color(0xFF5B1A00)
    val Ember30 = Color(0xFF8A2E0E)
    val Ember40 = Color(0xFFB4401A)
    val Ember80 = Color(0xFFFFB59A)
    val Ember90 = Color(0xFFFFDCCF)

    // Ink (neutrals with a violet tint)
    val Ink0 = Color(0xFF000000)
    val Ink6 = Color(0xFF0F0D16)
    val Ink10 = Color(0xFF16131F)
    val Ink15 = Color(0xFF1E1A2A)
    val Ink20 = Color(0xFF2A2638)
    val Ink30 = Color(0xFF4A4560)
    val Ink50 = Color(0xFF7A7590)
    val Ink60 = Color(0xFF8F89A6)
    val Ink80 = Color(0xFFC0BAD3)
    val Ink90 = Color(0xFFE8E4F3)
    val Ink92 = Color(0xFFECE8F5)
    val Ink96 = Color(0xFFF7F5FC)
    val Ink100 = Color(0xFFFFFFFF)

    // Errors and status
    val Red20 = Color(0xFF690005)
    val Red40 = Color(0xFFBA1A1A)
    val Red80 = Color(0xFFFFB4AB)
    val Green400 = Color(0xFF4ADE80)
    val Green700 = Color(0xFF15803D)
    val Amber400 = Color(0xFFFBBF24)
    val Amber700 = Color(0xFFB45309)
    val Red400 = Color(0xFFF87171)
    val Red700 = Color(0xFFC81E1E)

    /** Colors the user can pick for a category (ARGB, stored as Long in the model). */
    val CategoryPalette: List<Long> = listOf(
        0xFF3B82F6, 0xFF8B5CF6, 0xFFF59E0B, 0xFFF97316, 0xFFEF4444, 0xFF14B8A6,
        0xFF10B981, 0xFF64748B, 0xFFEC4899, 0xFF06B6D4, 0xFF84CC16, 0xFFA855F7,
    )
}

/** Status colors not covered by Material 3 roles. */
data class FluxStatusColors(
    val success: Color,
    val warning: Color,
    val danger: Color,
)

val LightStatusColors = FluxStatusColors(
    success = FluxColorTokens.Green700,
    warning = FluxColorTokens.Amber700,
    danger = FluxColorTokens.Red700,
)

val DarkStatusColors = FluxStatusColors(
    success = FluxColorTokens.Green400,
    warning = FluxColorTokens.Amber400,
    danger = FluxColorTokens.Red400,
)
