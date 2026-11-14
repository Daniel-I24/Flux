package com.flux.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Colors shared by every theme: status, category choices and pure black/white. The theme colors
 * live in [AndeanSunset] and [NightJungle] (v1.2, replacing "Aurora"); screens read the roles.
 */
object FluxColorTokens {
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)

    // Errors and status
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
