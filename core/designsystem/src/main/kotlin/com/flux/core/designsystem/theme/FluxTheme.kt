package com.flux.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.flux.core.model.ThemeMode

private val LocalStatusColors = staticCompositionLocalOf { LightStatusColors }

/** Phone theme: Material 3 with FLUX tokens; [themeMode] comes from user settings. */
@Composable
fun FluxTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    CompositionLocalProvider(LocalStatusColors provides if (dark) DarkStatusColors else LightStatusColors) {
        MaterialTheme(
            colorScheme = if (dark) FluxDarkColorScheme else FluxLightColorScheme,
            typography = FluxTypography,
            shapes = FluxShapes,
            content = content,
        )
    }
}

object FluxThemeExtras {
    val status: FluxStatusColors
        @Composable
        @ReadOnlyComposable
        get() = LocalStatusColors.current
}
