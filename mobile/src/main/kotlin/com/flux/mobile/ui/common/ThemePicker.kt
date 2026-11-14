package com.flux.mobile.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.designsystem.theme.roles
import com.flux.core.model.ThemePalette
import com.flux.mobile.R

fun ThemePalette.labelRes(): Int = when (this) {
    ThemePalette.ANDEAN_SUNSET -> R.string.theme_andean_sunset
    ThemePalette.NIGHT_JUNGLE -> R.string.theme_night_jungle
}

private fun ThemePalette.descriptionRes(): Int = when (this) {
    ThemePalette.ANDEAN_SUNSET -> R.string.theme_andean_sunset_description
    ThemePalette.NIGHT_JUNGLE -> R.string.theme_night_jungle_description
}

/** One card per theme with its three accent colors; the choice is shared with the watch. */
@Composable
fun ThemePicker(selected: ThemePalette, onSelect: (ThemePalette) -> Unit, dark: Boolean, modifier: Modifier = Modifier) {
    Column(modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
        ThemePalette.entries.forEach { palette ->
            val roles = palette.roles().let { if (dark) it.dark else it.light }
            val chosen = palette == selected
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().selectable(selected = chosen, role = Role.RadioButton, onClick = { onSelect(palette) }),
                border = BorderStroke(
                    if (chosen) FluxSizes.selectedBorder else FluxSizes.hairline,
                    if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                ),
            ) {
                Row(
                    Modifier.background(roles.surface).padding(FluxSpacing.md).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(FluxSpacing.md),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.xxs)) {
                        listOf(roles.primary, roles.secondary, roles.tertiary).forEach { color ->
                            Box(Modifier.size(FluxSizes.swatch).background(color, CircleShape))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(palette.labelRes()), style = MaterialTheme.typography.titleMedium, color = roles.onSurface)
                        Text(
                            stringResource(palette.descriptionRes()),
                            style = MaterialTheme.typography.bodySmall,
                            color = roles.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
