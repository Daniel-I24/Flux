package com.flux.mobile.feature.categories

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.designsystem.category.color
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.category.toImageVector
import com.flux.core.designsystem.component.FluxLoadingState
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Category
import com.flux.mobile.R
import com.flux.mobile.feature.home.CategoryCount

/** Categorías: built-in and user categories with color, icon, sound and pending count. */
@Composable
fun CategoriesScreen(snackbar: SnackbarHostState, modifier: Modifier = Modifier, viewModel: CategoriesViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var deletingId by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(message) {
        message?.let {
            viewModel.consumeMessage()
            snackbar.showSnackbar(context.getString(it))
        }
    }
    if (state.loading) {
        FluxLoadingState(modifier)
        return
    }

    Box(modifier.fillMaxSize()) {
        CategoryList(state.items, onEdit = { editingId = it }, onDelete = { deletingId = it })
        ExtendedFloatingActionButton(
            onClick = { creating = true },
            icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.categories_new)) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(FluxSpacing.lg),
        )
    }

    val editing = state.items.firstOrNull { it.category.id == editingId }?.category
    if (editing != null || creating) {
        CategoryEditorDialog(
            category = editing,
            onSave = { draft, defaultNames ->
                viewModel.save(draft, defaultNames) {
                    editingId = null
                    creating = false
                }
            },
            onDismiss = {
                editingId = null
                creating = false
            },
        )
    }
    state.items.firstOrNull { it.category.id == deletingId }?.let { (category, pending) ->
        DeleteCategoryDialog(category, pending, onConfirm = {
            viewModel.delete(category.id)
            deletingId = null
        }, onDismiss = { deletingId = null })
    }
}

@Composable
private fun CategoryList(items: List<CategoryCount>, onEdit: (String) -> Unit, onDelete: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(bottom = FluxSizes.fab + FluxSpacing.xxl)) {
        items(items, key = { it.category.id }) { (category, pending) ->
            ListItem(
                modifier = Modifier.clickable { onEdit(category.id) },
                leadingContent = {
                    Icon(category.icon.toImageVector(), null, tint = category.color, modifier = Modifier.size(FluxSizes.iconLg))
                },
                headlineContent = { Text(category.displayName()) },
                supportingContent = { Text(pluralStringResource(R.plurals.categories_pending, pending, pending)) },
                trailingContent = {
                    if (!category.isDefault) {
                        IconButton(onClick = { onDelete(category.id) }) {
                            Icon(Icons.Rounded.Delete, stringResource(R.string.categories_delete, category.displayName()))
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun DeleteCategoryDialog(category: Category, pending: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.categories_delete, category.displayName())) },
        text = { Text(pluralStringResource(R.plurals.categories_delete_message, pending, pending)) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(stringResource(R.string.categories_delete_confirm)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}
