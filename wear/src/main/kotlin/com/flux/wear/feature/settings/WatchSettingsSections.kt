package com.flux.wear.feature.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.Text
import com.flux.core.model.ThemePalette
import com.flux.wear.R

fun ThemePalette.labelRes(): Int = when (this) {
    ThemePalette.ANDEAN_SUNSET -> R.string.theme_andean_sunset
    ThemePalette.NIGHT_JUNGLE -> R.string.theme_night_jungle
}

/** The theme shared with the phone: picking it here changes it there too. */
fun TransformingLazyColumnScope.themeSection(selected: ThemePalette, onSelect: (ThemePalette) -> Unit) {
    item { ListHeader { Text(stringResource(R.string.settings_theme)) } }
    items(ThemePalette.entries, key = { it.name }) { palette ->
        RadioButton(
            selected = palette == selected,
            onSelect = { onSelect(palette) },
            label = { Text(stringResource(palette.labelRes())) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

fun TransformingLazyColumnScope.linkSection(state: WatchSettingsUiState, onLink: () -> Unit, onUnlink: () -> Unit) {
    item { ListHeader { Text(stringResource(R.string.settings_link)) } }
    item {
        val label = when {
            state.linkedAccount == null -> stringResource(R.string.settings_not_linked)
            state.phone.userName != null -> stringResource(R.string.settings_linked_to, state.phone.userName)
            else -> stringResource(R.string.settings_linked_unknown)
        }
        Text(label, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
    item {
        val linked = state.linkedAccount != null
        OutlinedButton(
            onClick = if (linked) onUnlink else onLink,
            label = { Text(stringResource(if (linked) R.string.settings_unlink else R.string.settings_link_phone)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
