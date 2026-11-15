package com.flux.core.designsystem.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

/** Buttons and cards sink slightly while pressed (a still button when animations are reduced). */
@Composable
fun Modifier.pressScale(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reduced = rememberReducedMotion()
    val scale by animateFloatAsState(
        targetValue = if (pressed && !reduced) FluxMotion.PRESSED_SCALE else 1f,
        animationSpec = if (reduced) snap() else spring(dampingRatio = FluxMotion.PRESS_BOUNCE),
        label = "press",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * List entrance: each item fades and rises once, a little after the one above it (capped, so long
 * lists are not slow). Runs on the render thread through graphicsLayer: no recomposition per frame.
 */
@Composable
fun Modifier.enterOnce(index: Int): Modifier {
    val reduced = rememberReducedMotion()
    val progress = remember { Animatable(if (reduced) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (reduced) return@LaunchedEffect
        delay(index.coerceAtMost(FluxMotion.STAGGER_MAX_ITEMS) * FluxMotion.STAGGER_MS)
        progress.animateTo(1f, tween(FluxMotion.MEDIUM_MS))
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * FluxMotion.ENTER_OFFSET_PX
    }
}
