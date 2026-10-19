package com.flux.wear.feature.link

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.wear.R
import com.flux.wear.sync.PendingLink

/** The same 6-digit code is shown on the phone; the user confirms it here. */
@Composable
fun LinkConfirmContent(pending: PendingLink, onAnswer: (Boolean) -> Unit) {
    val spokenCode = pending.code.toCharArray().joinToString(" ")
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.link_confirm_title), style = MaterialTheme.typography.titleSmall)
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
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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
