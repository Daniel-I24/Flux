package com.flux.mobile.feature.notes

import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.domain.usecase.NoteQuery
import com.flux.core.domain.usecase.NoteSort
import com.flux.core.model.Category
import com.flux.mobile.R
import com.flux.mobile.feature.home.CategoryDot

@get:StringRes
private val NoteSort.label: Int
    get() = when (this) {
        NoteSort.NEWEST -> R.string.notes_sort_newest
        NoteSort.DUE_DATE -> R.string.notes_sort_due
        NoteSort.CATEGORY -> R.string.notes_sort_category
    }

@Composable
fun NotesFilterBar(
    query: NoteQuery,
    categories: List<Category>,
    onText: (String) -> Unit,
    onCategory: (String?) -> Unit,
    onSort: (NoteSort) -> Unit,
    onIncludeCompleted: (Boolean) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = FluxSpacing.lg, vertical = FluxSpacing.sm)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query.text,
                onValueChange = onText,
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text(stringResource(R.string.notes_search)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.text.isNotEmpty()) {
                        IconButton(onClick = { onText("") }) { Icon(Icons.Rounded.Clear, stringResource(R.string.notes_search_clear)) }
                    }
                },
            )
            SortMenu(query.sort, onSort)
        }
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = FluxSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm),
        ) {
            FilterChip(
                selected = !query.includeCompleted,
                onClick = { onIncludeCompleted(!query.includeCompleted) },
                label = { Text(stringResource(R.string.notes_only_pending)) },
            )
            FilterChip(selected = query.categoryId == null, onClick = {
                onCategory(null)
            }, label = { Text(stringResource(R.string.notes_all)) })
            categories.forEach { category ->
                FilterChip(
                    selected = query.categoryId == category.id,
                    onClick = { onCategory(category.id.takeUnless { it == query.categoryId }) },
                    label = { Text(category.displayName()) },
                    leadingIcon = { CategoryDot(category) },
                )
            }
        }
    }
}

@Composable
private fun SortMenu(sort: NoteSort, onSort: (NoteSort) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = stringResource(R.string.notes_sort, stringResource(sort.label)))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            NoteSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.label)) },
                    onClick = {
                        onSort(option)
                        open = false
                    },
                    trailingIcon = { if (option == sort) Icon(Icons.Rounded.Check, contentDescription = null) },
                )
            }
        }
    }
}
