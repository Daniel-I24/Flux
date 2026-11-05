package com.flux.core.designsystem.component

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import com.flux.core.designsystem.motion.rememberReducedMotion
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Animated "lava" of theme colors behind the microphone, reacting to the voice level (0–1 from
 * onRmsChanged). AGSL on Android 13+, Canvas gradients before. It only animates while [active]
 * and stays still when the user reduced animations. [level] is read in the draw phase, so voice
 * updates never recompose the screen.
 */
@Composable
fun VoiceLava(active: Boolean, level: () -> Float, colors: LavaColors, modifier: Modifier = Modifier) {
    val reduced = rememberReducedMotion()
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(active, reduced) {
        if (!active || reduced) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { now -> time.floatValue = (now - start) / NANOS_PER_SECOND }
        }
    }
    val lava = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Modifier.drawWithCache {
            val shader = RuntimeShader(LAVA_SHADER)
            val brush = ShaderBrush(shader)
            onDrawBehind {
                shader.bind(size.width, size.height, time.floatValue, if (active) level() else 0f, colors)
                drawRect(brush)
            }
        }
    } else {
        Modifier.drawWithCache { onDrawBehind { drawCanvasLava(time.floatValue, if (active) level() else 0f, colors) } }
    }
    Box(modifier.then(lava))
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun RuntimeShader.bind(width: Float, height: Float, time: Float, level: Float, colors: LavaColors) {
    setFloatUniform("size", width, height)
    setFloatUniform("time", time)
    setFloatUniform("level", level.coerceIn(0f, 1f))
    setColorUniform("c1", colors.first.toArgb())
    setColorUniform("c2", colors.second.toArgb())
    setColorUniform("c3", colors.third.toArgb())
    setColorUniform("bg", colors.background.toArgb())
}

/** Same three moving blobs as the shader, with radial gradients (Android 12 and older, and the watch < 33). */
private fun DrawScope.drawCanvasLava(time: Float, level: Float, colors: LavaColors) {
    drawRect(colors.background)
    val unit = min(size.width, size.height)
    val radius = unit * (BASE_RADIUS + LEVEL_RADIUS * level.coerceIn(0f, 1f))
    BLOBS.forEachIndexed { index, blob ->
        val color = listOf(colors.first, colors.second, colors.third)[index]
        val center = Offset(
            size.width / 2 + unit * blob.amplitude * sin(time * blob.speedX + blob.phase),
            size.height / 2 + unit * blob.amplitude * cos(time * blob.speedY + blob.phase),
        )
        drawCircle(Brush.radialGradient(listOf(color, color.copy(alpha = 0f)), center, radius), radius, center)
    }
}

private data class Blob(val amplitude: Float, val speedX: Float, val speedY: Float, val phase: Float)

private val BLOBS = listOf(Blob(0.28f, 0.7f, 0.9f, 0f), Blob(0.26f, 0.6f, 0.8f, 1.7f), Blob(0.24f, 1.1f, 0.5f, 3.1f))
private const val BASE_RADIUS = 0.45f
private const val LEVEL_RADIUS = 0.3f
private const val NANOS_PER_SECOND = 1_000_000_000f
