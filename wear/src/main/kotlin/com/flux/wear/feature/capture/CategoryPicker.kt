package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.flux.core.designsystem.category.displayName
import com.flux.core.model.Category
import com.flux.wear.R
import com.flux.wear.ui.CategoryIconDot

/** Rotary-scrollable list to correct the AI's category; the choice is remembered. */
@Composable
fun CategoryPicker(categories: List<Category>, selectedId: String, onSelected: (String) -> Unit) {
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ListHeader { Text(stringResource(R.string.capture_change_category)) } }
            items(categories, key = { it.id }) { category ->
                RadioButton(
                    selected = category.id == selectedId,
                    onSelect = { onSelected(category.id) },
                    icon = { CategoryIconDot(category) },
                    label = { Text(category.displayName()) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
