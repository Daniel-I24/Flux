package com.flux.mobile.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.category.labelRes
import com.flux.core.designsystem.category.nameRes
import com.flux.core.designsystem.category.toImageVector
import com.flux.core.designsystem.theme.FluxColorTokens
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.domain.usecase.CategoryDraft
import com.flux.core.model.Category
import com.flux.core.model.CategoryIcon
import com.flux.core.model.DefaultCategory
import com.flux.mobile.R
import com.flux.mobile.ui.sound.SoundPicker

/** Create ([category] = null) or edit a category: name, color, icon and reminder sound. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryEditorDialog(category: Category?, onSave: (CategoryDraft, Map<String, String>) -> Unit, onDismiss: () -> Unit) {
    val currentName = category?.displayName().orEmpty()
    var name by rememberSaveable { mutableStateOf(currentName) }
    var color by rememberSaveable { mutableStateOf(category?.colorArgb ?: FluxColorTokens.CategoryPalette.first()) }
    var icon by rememberSaveable { mutableStateOf(category?.icon ?: CategoryIcon.STAR) }
    var sound by remember(category?.id) { mutableStateOf(category?.sound) }
    val defaultNames = DefaultCategory.entries.associate { it.id to stringResource(it.nameRes()) }
    val nameValid = name.isNotBlank() && name.length <= Category.MAX_NAME_LENGTH

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (category == null) R.string.categories_new else R.string.categories_edit)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(Category.MAX_NAME_LENGTH) },
                    label = { Text(stringResource(R.string.categories_name)) },
                    singleLine = true,
                    isError = !nameValid,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.categories_color), style = MaterialTheme.typography.titleSmall)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(FluxSpacing.sm)
                ) {
                    FluxColorTokens.CategoryPalette.forEachIndexed { index, argb ->
                        ColorSwatch(argb, selected = argb == color, label = stringResource(R.string.categories_color_option, index + 1)) {
                            color =
                                argb
                        }
                    }
                }
                Text(stringResource(R.string.categories_icon), style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.xs)) {
                    CategoryIcon.entries.forEach { option ->
                        FilledIconToggleButton(checked = option == icon, onCheckedChange = { icon = option }) {
                            Icon(option.toImageVector(), contentDescription = stringResource(option.labelRes()))
                        }
                    }
                }
                Text(stringResource(R.string.categories_sound), style = MaterialTheme.typography.titleSmall)
                SoundPicker(selected = sound, onSelect = { sound = it }, defaultLabel = stringResource(R.string.sound_use_default))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(CategoryDraft(category?.id, name, color, icon, sound), defaultNames) },
                enabled = nameValid,
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } },
    )
}

@Composable
private fun ColorSwatch(argb: Long, selected: Boolean, label: String, onClick: () -> Unit) {
    val ring = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent
    Box(
        Modifier
            .size(FluxSizes.minTouchTarget)
            .border(FluxSizes.borderThin * SELECTED_RING, ring, CircleShape)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .semantics { contentDescription = label },
    ) {
        Box(Modifier.size(FluxSizes.badge).align(Alignment.Center).background(Color(argb), CircleShape))
    }
}

private const val SELECTED_RING = 3
