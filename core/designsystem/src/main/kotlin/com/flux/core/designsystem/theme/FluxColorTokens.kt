package com.flux.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/** Raw palette. Screens never use these directly: they read MaterialTheme.colorScheme. */
object FluxColorTokens {
    val Indigo50 = Color(0xFFEEF2FF)
    val Indigo200 = Color(0xFFC7D2FE)
    val Indigo300 = Color(0xFFA5B4FC)
    val Indigo500 = Color(0xFF6366F1)
    val Indigo600 = Color(0xFF4F46E5)
    val Indigo900 = Color(0xFF1E1B4B)
    val Cyan300 = Color(0xFF67E8F9)
    val Cyan600 = Color(0xFF0891B2)
    val Cyan900 = Color(0xFF164E63)
    val Slate50 = Color(0xFFF4F6FB)
    val Slate100 = Color(0xFFECEFF5)
    val Slate200 = Color(0xFFDADFE9)
    val Slate400 = Color(0xFF9CA8BC)
    val Slate600 = Color(0xFF4B5563)
    val Slate800 = Color(0xFF1B202E)
    val Slate850 = Color(0xFF151A26)
    val Slate900 = Color(0xFF111520)
    val Slate950 = Color(0xFF090B11)
    val Red400 = Color(0xFFF87171)
    val Red700 = Color(0xFFC81E1E)
    val Green400 = Color(0xFF4ADE80)
    val Green700 = Color(0xFF15803D)
    val Amber400 = Color(0xFFFBBF24)
    val Amber700 = Color(0xFFB45309)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

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
