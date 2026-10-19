package com.flux.mobile.feature.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.designsystem.theme.FluxThemeExtras
import com.flux.core.model.WatchLink
import com.flux.mobile.R

/** Shows the watch link state with the one action that fixes each situation. */
@Composable
fun WatchLinkPanel(modifier: Modifier = Modifier, viewModel: LinkViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Card(modifier) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
            when (val link = state.link) {
                WatchLink.Searching -> Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md)
                ) {
                    CircularProgressIndicator()
                    Text(stringResource(R.string.link_searching))
                }
                WatchLink.NoWatchFound -> Explanation(stringResource(R.string.link_no_watch)) {
                    TextButton(onClick = viewModel::refresh) { Text(stringResource(R.string.link_retry)) }
                }
                is WatchLink.AppMissingOnWatch -> Explanation(stringResource(R.string.link_app_missing, link.displayName)) {
                    Button(onClick = { viewModel.install(link.nodeId) }) { Text(stringResource(R.string.link_install_on_watch)) }
                    TextButton(onClick = viewModel::refresh) { Text(stringResource(R.string.link_retry)) }
                }
                is WatchLink.Available -> Explanation(stringResource(R.string.link_available, link.displayName)) {
                    Button(onClick = { viewModel.link(link.nodeId, link.displayName) }) { Text(stringResource(R.string.link_start)) }
                }
                is WatchLink.AwaitingConfirmation -> ConfirmationCode(link.code)
                is WatchLink.Linked -> LinkedWatch(link, onUnlink = viewModel::unlink)
                WatchLink.WatchLinkedToOtherAccount -> Explanation(stringResource(R.string.link_other_account)) {
                    TextButton(onClick = viewModel::refresh) { Text(stringResource(R.string.link_retry)) }
                }
                WatchLink.UpdateRequired -> Explanation(stringResource(R.string.link_update_required)) {}
            }
            state.problem?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun Explanation(text: String, actions: @Composable () -> Unit) {
    Text(text, style = MaterialTheme.typography.bodyMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) { actions() }
}

@Composable
private fun ConfirmationCode(code: String) {
    val description = stringResource(R.string.link_code_description, code.toCharArray().joinToString(" "))
    Text(stringResource(R.string.link_code_title), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.link_code_message), style = MaterialTheme.typography.bodyMedium)
    Text(
        text = code.chunked(CODE_GROUP).joinToString(" "),
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.semantics { contentDescription = description },
    )
}

@Composable
private fun LinkedWatch(link: WatchLink.Linked, onUnlink: () -> Unit) {
    Text(stringResource(R.string.link_linked, link.displayName), style = MaterialTheme.typography.titleMedium)
    Text(
        text = stringResource(if (link.connected) R.string.link_status_connected else R.string.link_status_disconnected),
        color = if (link.connected) FluxThemeExtras.status.success else FluxThemeExtras.status.warning,
    )
    OutlinedButton(onClick = onUnlink) { Text(stringResource(R.string.link_unlink)) }
}

private const val CODE_GROUP = 3
