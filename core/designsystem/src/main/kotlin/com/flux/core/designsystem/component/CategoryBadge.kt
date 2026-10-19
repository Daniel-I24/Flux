package com.flux.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.flux.core.designsystem.category.color
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.category.toImageVector
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Category

private const val BADGE_BACKGROUND_ALPHA = 0.16f

/** Category pill: color + icon + name (color is never the only cue). */
@Composable
fun CategoryBadge(category: Category, modifier: Modifier = Modifier) {
    val tint = category.color
    Surface(color = tint.copy(alpha = BADGE_BACKGROUND_ALPHA), shape = CircleShape, modifier = modifier) {
        Row(
            modifier = Modifier.padding(horizontal = FluxSpacing.sm, vertical = FluxSpacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FluxSpacing.xs),
        ) {
            Icon(category.icon.toImageVector(), contentDescription = null, tint = tint, modifier = Modifier.size(FluxSizes.iconSm))
            Text(category.displayName(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
