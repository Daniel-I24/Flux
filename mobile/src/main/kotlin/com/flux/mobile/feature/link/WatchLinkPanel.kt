package com.flux.mobile.feature.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

/** Ajustes > Reloj: the state, and always the way out: link, unlink or switch to another watch. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WatchLinkPanel(modifier: Modifier = Modifier, viewModel: LinkViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmUnlink by rememberSaveable { mutableStateOf(false) }
    val link = state.link
    Card(modifier) {
        Column(Modifier.padding(FluxSpacing.lg), verticalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
            LinkStatus(link)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm),
            ) {
                when (link) {
                    is WatchLink.Linked -> {
                        OutlinedButton(onClick = viewModel::choose) { Text(stringResource(R.string.link_change)) }
                        OutlinedButton(onClick = { confirmUnlink = true }) { Text(stringResource(R.string.link_unlink)) }
                    }
                    is WatchLink.AwaitingConfirmation, WatchLink.UpdateRequired, WatchLink.Searching -> Unit
                    else -> Button(onClick = viewModel::choose) { Text(stringResource(R.string.link_start)) }
                }
            }
            state.problem?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
        }
    }
    if (state.choosing) {
        WatchChooserDialog(
            candidates = state.candidates,
            replacing = link is WatchLink.Linked,
            searching = link == WatchLink.Searching,
            actions = ChooserActions(onLink = viewModel::link, onInstall = viewModel::install, onSearch = viewModel::refresh),
            onDismiss = viewModel::closeChooser,
        )
    }
    if (confirmUnlink) {
        UnlinkDialog(
            onConfirm = {
                confirmUnlink = false
                viewModel.unlink()
            },
            onDismiss = { confirmUnlink = false },
        )
    }
}

@Composable
private fun LinkStatus(link: WatchLink) {
    when (link) {
        WatchLink.Searching -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md),
        ) {
            CircularProgressIndicator()
            Text(stringResource(R.string.link_searching))
        }
        WatchLink.NoWatchFound -> Text(stringResource(R.string.link_no_watch), style = MaterialTheme.typography.bodyMedium)
        is WatchLink.AppMissingOnWatch -> Text(stringResource(R.string.link_app_missing, link.displayName))
        is WatchLink.Available -> Text(stringResource(R.string.link_available, link.displayName))
        is WatchLink.AwaitingConfirmation -> ConfirmationCode(link.code)
        is WatchLink.Linked -> LinkedWatch(link)
        WatchLink.WatchLinkedToOtherAccount -> Text(stringResource(R.string.link_other_account))
        WatchLink.UpdateRequired -> Text(stringResource(R.string.link_update_required))
    }
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
private fun LinkedWatch(link: WatchLink.Linked) {
    Text(stringResource(R.string.link_linked, link.watchName()), style = MaterialTheme.typography.titleMedium)
    Text(
        text = stringResource(if (link.connected) R.string.link_status_connected else R.string.link_status_disconnected),
        color = if (link.connected) FluxThemeExtras.status.success else FluxThemeExtras.status.warning,
    )
}

/** A linked watch that is off or away has no name from the Data Layer. */
@Composable
fun WatchLink.Linked.watchName(): String = displayName.ifBlank { stringResource(R.string.link_unknown_watch) }

private const val CODE_GROUP = 3
