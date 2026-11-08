package com.flux.mobile.feature.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Watch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R
import com.flux.mobile.link.WatchCandidate

/**
 * Every watch paired with this phone. With FLUX it links right away; without it, FLUX opens the
 * store on the watch. With none, it explains the system pairing (Galaxy Wearable, Pixel Watch).
 */
@Composable
fun WatchChooserDialog(
    candidates: List<WatchCandidate>,
    replacing: Boolean,
    searching: Boolean,
    actions: ChooserActions,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (replacing) R.string.link_change else R.string.link_chooser_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
                when {
                    searching -> LinearProgressIndicator(Modifier.fillMaxWidth())
                    candidates.isEmpty() -> Text(stringResource(R.string.link_chooser_empty))
                    else -> candidates.forEach { CandidateRow(it, actions) }
                }
            }
        },
        confirmButton = { TextButton(onClick = actions.onSearch, enabled = !searching) { Text(stringResource(R.string.link_retry)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun CandidateRow(candidate: WatchCandidate, actions: ChooserActions) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm),
    ) {
        Icon(Icons.Rounded.Watch, contentDescription = null)
        Column(Modifier.weight(1f)) {
            Text(candidate.displayName, style = MaterialTheme.typography.bodyLarge)
            Text(
                stringResource(if (candidate.hasApp) R.string.link_candidate_ready else R.string.link_candidate_no_app),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (candidate.hasApp) {
            TextButton(onClick = { actions.onLink(candidate) }) { Text(stringResource(R.string.link_choose)) }
        } else {
            TextButton(onClick = { actions.onInstall(candidate) }) { Text(stringResource(R.string.link_install_on_watch)) }
        }
    }
}

@Composable
fun UnlinkDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.link_unlink_title)) },
        text = { Text(stringResource(R.string.link_unlink_message)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.link_unlink)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
