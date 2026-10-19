package com.flux.mobile.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R
import com.flux.mobile.feature.link.WatchLinkPanel

/** Ajustes: every setting of FLUX lives here, grouped in sections. */
@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(FluxSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(FluxSpacing.md),
    ) {
        Text(
            stringResource(R.string.settings_watch),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics {
                heading()
            }
        )
        WatchLinkPanel()
    }
}
