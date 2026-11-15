package com.flux.wear.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.wear.compose.material3.MaterialTheme
import com.flux.core.designsystem.motion.FluxMotion
import com.flux.wear.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Opening animation: dots spin in from the center and close into a ring in the theme's colors,
 * then [onDone] shows the home screen. Drawn in the draw phase only (no recomposition per frame).
 */
@Composable
fun WatchIntro(onDone: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val label = stringResource(R.string.intro_description)
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(FluxMotion.LONG_MS, easing = FastOutSlowInEasing))
        onDone()
    }
    Canvas(Modifier.fillMaxSize().padding(WatchDimens.introPadding).semantics { contentDescription = label }) {
        val t = progress.value
        val radius = size.minDimension / 2 * t
        val turn = (1f - t) * MAX_TURN
        val dot = WatchDimens.introDot.toPx()
        repeat(DOTS) { index ->
            val angle = (index.toFloat() / DOTS + turn) * FULL_CIRCLE
            val position = center + Offset(cos(angle) * radius, sin(angle) * radius)
            drawCircle(if (index % 2 == 0) primary else tertiary, radius = dot, center = position, alpha = t)
        }
        // The dots meet: the ring appears in the last part of the animation.
        val ring = ((t - RING_START) / (1f - RING_START)).coerceIn(0f, 1f)
        if (ring > 0f) drawCircle(primary, radius = radius, center = center, alpha = ring, style = Stroke(dot / 2))
    }
}

private const val DOTS = 8
private const val MAX_TURN = 0.75f
private const val RING_START = 0.7f
private const val FULL_CIRCLE = (2 * PI).toFloat()
