package com.flux.mobile.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

/** Greeting over the theme's gradient (terracotta to peach, or jungle green to orchid) and what is coming up. */
@Composable
fun HomeHero(userName: String?, upcoming: Int, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(Brush.linearGradient(listOf(colors.primary, colors.tertiary)))
            .padding(FluxSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(FluxSpacing.xs),
    ) {
        Text(
            text = userName?.let { stringResource(R.string.home_greeting, it) } ?: stringResource(R.string.home_greeting_anonymous),
            style = MaterialTheme.typography.headlineMedium,
            color = colors.onPrimary,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = if (upcoming == 0) {
                stringResource(R.string.home_hero_none)
            } else {
                pluralStringResource(R.plurals.home_hero_upcoming, upcoming, upcoming)
            },
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onPrimary,
        )
    }
}
