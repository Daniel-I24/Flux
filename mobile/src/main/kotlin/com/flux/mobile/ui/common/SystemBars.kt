package com.flux.mobile.ui.common

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.flux.core.model.ThemeMode

/** System bar icons follow FLUX's theme, not the phone's: light icons on a dark FLUX theme and the other way round. */
@Composable
fun ComponentActivity.ThemedSystemBars(mode: ThemeMode) {
    val dark = mode.isDark(isSystemInDarkTheme())
    DisposableEffect(dark) {
        val style = if (dark) SystemBarStyle.dark(Color.TRANSPARENT) else SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        onDispose { }
    }
}
