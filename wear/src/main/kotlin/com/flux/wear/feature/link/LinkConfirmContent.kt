package com.flux.wear.feature.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.wear.R
import com.flux.wear.sync.PendingLink
import com.flux.wear.ui.WatchDimens
import com.flux.wear.ui.rememberWatchInsets

/** The same 6-digit code is shown on the phone; the user confirms it here. */
@Composable
fun LinkConfirmContent(pending: PendingLink, onAnswer: (Boolean) -> Unit) {
    val spokenCode = pending.code.toCharArray().joinToString(" ")
    Column(
        // Scrolls on small screens; round-aware margins keep the code and buttons off the edge.
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(rememberWatchInsets()),
        verticalArrangement = Arrangement.spacedBy(WatchDimens.gap, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.link_confirm_title), style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
        Text(
            text =
            pending.userName?.let { stringResource(R.string.link_confirm_message, it) }
                ?: stringResource(R.string.link_confirm_message_anonymous),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = pending.code.chunked(CODE_GROUP).joinToString(" "),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { contentDescription = spokenCode },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(WatchDimens.gapButtons)) {
            IconButton(onClick = { onAnswer(false) }) {
                Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.link_confirm_reject))
            }
            FilledIconButton(onClick = { onAnswer(true) }) {
                Icon(Icons.Rounded.Check, contentDescription = stringResource(R.string.link_confirm_accept))
            }
        }
    }
}

private const val CODE_GROUP = 3
