package com.flux.core.designsystem.motion

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Durations shared by both apps so motion feels the same on the phone and the watch. */
object FluxMotion {
    const val SHORT_MS = 150
    const val MEDIUM_MS = 280
    const val LONG_MS = 450
    const val LIST_STAGGER_MS = 40
    const val LIST_STAGGER_MAX_ITEMS = 8
}

/** True when the user turned animations off ("Quitar animaciones" / animator scale 0). */
fun Context.isReducedMotion(): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) { context.isReducedMotion() }
}
