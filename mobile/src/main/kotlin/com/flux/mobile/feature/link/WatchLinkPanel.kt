package com.flux.mobile.feature.link

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.mobile.R

/** Watch linking panel (detection, 6-digit code and confirmation are added with the link feature). */
@Composable
fun WatchLinkPanel(modifier: Modifier = Modifier) {
    Text(stringResource(R.string.watch_step_subtitle), style = MaterialTheme.typography.bodyMedium, modifier = modifier)
}
