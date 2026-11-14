package com.flux.wear.feature.setup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.flux.wear.R

private val BRAND_STEPS = listOf(R.string.setup_double_press_galaxy, R.string.setup_double_press_pixel, R.string.setup_double_press_other)

/** Opening FLUX from the button is a system setting: the steps for each brand (tile and complication keep dictating). */
fun TransformingLazyColumnScope.doublePressGuide() {
    item { ListHeader { Text(stringResource(R.string.setup_double_press_title)) } }
    BRAND_STEPS.forEach { steps ->
        item {
            Text(
                stringResource(steps),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
