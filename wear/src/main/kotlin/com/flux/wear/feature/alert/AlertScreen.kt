package com.flux.wear.feature.alert

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.core.model.Note
import com.flux.wear.R
import com.flux.wear.ui.rememberHaptics

@Composable
fun AlertScreen(note: Note?, onComplete: () -> Unit, onSnooze: () -> Unit) {
    val haptics = rememberHaptics()
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Rounded.NotificationsActive,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = note?.title ?: stringResource(R.string.alert_subtitle),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            maxLines = 3,
        )
        Button(
            onClick = {
                haptics.confirm()
                onComplete()
            },
            icon = { Icon(Icons.Rounded.Check, contentDescription = null) },
            label = { Text(stringResource(R.string.alert_complete)) },
            modifier = Modifier.fillMaxWidth(),
        )
        FilledTonalButton(
            onClick = {
                haptics.tap()
                onSnooze()
            },
            icon = { Icon(Icons.Rounded.Snooze, contentDescription = null) },
            label = { Text(stringResource(R.string.alert_snooze)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
