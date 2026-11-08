package com.flux.mobile.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.category.displayName
import com.flux.core.designsystem.component.CategoryBadge
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.model.Ambiguity
import com.flux.core.model.Category
import com.flux.core.model.Classification
import com.flux.mobile.R
import com.flux.mobile.feature.home.CategoryDot
import com.flux.mobile.ui.common.alarmConfirmation
import com.flux.mobile.ui.common.dueDateLabel

/** What FLUX understood; ambiguities as chips (the countdown waits for the answer). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CapturePreview(state: PhoneCaptureUi, classification: Classification, categories: List<Category>, viewModel: PhoneCaptureViewModel) {
    categories.firstOrNull { it.id == classification.categoryId }?.let { CategoryBadge(it) }
    Text(classification.title, style = MaterialTheme.typography.titleMedium)
    val question = classification.ambiguities.firstOrNull()
    val due = classification.dueAt
    if (question !is Ambiguity.ReminderTime) {
        Text(
            when {
                due == null -> stringResource(R.string.capture_no_reminder)
                classification.requiresAlarm -> alarmConfirmation(due)
                else -> dueDateLabel(due)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    if (question is Ambiguity.ReminderTime) {
        ReminderQuestion(question, onAnswer = viewModel::answerReminder)
    } else if (question != null) {
        Text(
            stringResource(if (question is Ambiguity.DueTime) R.string.capture_question_time else R.string.capture_question_category),
            style = MaterialTheme.typography.titleSmall,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            when (question) {
                is Ambiguity.DueTime -> question.options.forEach { option ->
                    FilterChip(selected = false, onClick = { viewModel.answerTime(option) }, label = { Text(dueDateLabel(option)) })
                }
                is Ambiguity.ReminderTime -> Unit
                is Ambiguity.CategoryChoice -> question.options.forEach { id ->
                    val category = categories.firstOrNull { it.id == id }
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.answerCategory(id) },
                        label = { Text(category?.displayName() ?: id) },
                        leadingIcon = category?.let { { CategoryDot(it) } },
                    )
                }
            }
        }
    }
    if (state.counting) {
        LinearProgressIndicator(
            progress = { state.secondsLeft / PhoneCaptureUi.AUTO_SAVE_SECONDS.toFloat() },
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.capture_saving_in, state.secondsLeft), style = MaterialTheme.typography.labelMedium)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
        OutlinedButton(onClick = viewModel::edit, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.capture_edit)) }
        Button(onClick = viewModel::save, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.action_save)) }
    }
}
