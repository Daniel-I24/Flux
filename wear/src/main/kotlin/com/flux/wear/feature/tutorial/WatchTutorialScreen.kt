package com.flux.wear.feature.tutorial

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOn
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TextButton
import com.flux.core.designsystem.motion.fluxPageTransition
import com.flux.core.designsystem.motion.rememberReducedMotion
import com.flux.wear.R
import com.flux.wear.ui.rememberWatchInsets

private data class TutorialPage(val icon: ImageVector, val title: Int, val body: Int)

private val PAGES = listOf(
    TutorialPage(Icons.Rounded.Mic, R.string.tutorial_watch_1_title, R.string.tutorial_watch_1_body),
    TutorialPage(Icons.Rounded.TaskAlt, R.string.tutorial_watch_2_title, R.string.tutorial_watch_2_body),
    TutorialPage(Icons.Rounded.AlarmOn, R.string.tutorial_watch_3_title, R.string.tutorial_watch_3_body),
)

/** Three short screens (voice, review, alarms); "Omitir" skips, Ajustes can show it again. */
@Composable
fun WatchTutorialScreen(onFinish: () -> Unit) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val reduced = rememberReducedMotion()
    AnimatedContent(targetState = page, label = "tutorial", transitionSpec = { fluxPageTransition(reduced) }) { index ->
        val current = PAGES[index]
        val last = index == PAGES.lastIndex
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(rememberWatchInsets()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        ) {
            Icon(current.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Text(stringResource(current.title), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(stringResource(current.body), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            Button(
                onClick = { if (last) onFinish() else page = index + 1 },
                label = { Text(stringResource(if (last) R.string.tutorial_start else R.string.tutorial_next)) },
                modifier = Modifier.fillMaxWidth(),
            )
            if (!last) TextButton(onClick = onFinish) { Text(stringResource(R.string.tutorial_skip)) }
        }
    }
}
