package com.flux.mobile.feature.tutorial

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.QuestionAnswer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.flux.core.designsystem.component.LavaColors
import com.flux.core.designsystem.component.VoiceLava
import com.flux.core.designsystem.motion.fluxPageTransition
import com.flux.core.designsystem.motion.rememberReducedMotion
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

private data class Step(val icon: ImageVector, val title: Int, val body: Int, val example: Int?)

private val STEPS = listOf(
    Step(Icons.Rounded.Mic, R.string.tutorial_1_title, R.string.tutorial_1_body, R.string.tutorial_1_example),
    Step(Icons.Rounded.QuestionAnswer, R.string.tutorial_2_title, R.string.tutorial_2_body, R.string.tutorial_2_example),
    Step(Icons.Rounded.AlarmOn, R.string.tutorial_3_title, R.string.tutorial_3_body, null),
    Step(Icons.Rounded.EventBusy, R.string.tutorial_4_title, R.string.tutorial_4_body, null),
)

/** Four steps with an example phrase; "Omitir" skips and Ajustes > Tutorial shows it again. */
@Composable
fun TutorialScreen(onFinish: () -> Unit) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val reduced = rememberReducedMotion()
    val scheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxSize()) {
        VoiceLava(
            active = !reduced,
            level = { LAVA_LEVEL },
            colors = LavaColors(scheme.primaryContainer, scheme.secondaryContainer, scheme.tertiaryContainer, scheme.background),
            modifier = Modifier.fillMaxSize(),
        )
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(FluxSpacing.xl)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.tutorial_progress, index + 1, STEPS.size), style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = onFinish) { Text(stringResource(R.string.tutorial_skip)) }
            }
            AnimatedContent(targetState = index, label = "tutorial", transitionSpec = {
                fluxPageTransition(reduced)
            }, modifier = Modifier.weight(1f)) {
                StepContent(STEPS[it])
            }
            val last = index == STEPS.lastIndex
            Button(onClick = { if (last) onFinish() else index++ }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(if (last) R.string.tutorial_start else R.string.tutorial_next))
            }
        }
    }
}

@Composable
private fun StepContent(step: Step) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FluxSpacing.lg, Alignment.CenterVertically),
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
            Icon(
                step.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(FluxSpacing.lg).size(FluxSizes.iconXl)
            )
        }
        Text(
            stringResource(step.title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(step.body), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        step.example?.let {
            Surface(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.large) {
                Text(stringResource(it), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(FluxSpacing.lg))
            }
        }
        Spacer(Modifier.size(FluxSpacing.md))
    }
}

private const val LAVA_LEVEL = 0.2f
