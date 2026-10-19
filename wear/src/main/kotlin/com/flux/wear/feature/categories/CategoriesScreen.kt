package com.flux.wear.feature.categories

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.flux.core.designsystem.category.displayName
import com.flux.wear.R
import com.flux.wear.ui.CategoryIconDot

/** Categories come from the phone (edited there); tapping one starts a dictation. */
@Composable
fun CategoriesScreen(onDictate: () -> Unit, viewModel: CategoriesViewModel = hiltViewModel()) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val listState = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = listState) { padding ->
        TransformingLazyColumn(state = listState, contentPadding = padding, modifier = Modifier.fillMaxSize()) {
            item { ListHeader { Text(stringResource(R.string.categories_title)) } }
            items(categories, key = { it.id }) { category ->
                FilledTonalButton(
                    onClick = onDictate,
                    icon = { CategoryIconDot(category) },
                    label = { Text(category.displayName()) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
