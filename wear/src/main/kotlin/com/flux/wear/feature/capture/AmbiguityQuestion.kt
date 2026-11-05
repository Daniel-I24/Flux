package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.flux.core.designsystem.category.displayName
import com.flux.core.model.Ambiguity
import com.flux.core.model.Category
import com.flux.wear.R
import com.flux.wear.ui.CategoryIconDot
import com.flux.wear.ui.rememberDueDateLabel

/** "¿A qué hora?" / "¿En qué categoría?" with one large button per option (glanceable, easy to tap). */
internal fun TransformingLazyColumnScope.ambiguityQuestion(
    question: Ambiguity,
    categories: List<Category>,
    onEvent: (CaptureEvent) -> Unit,
) {
    item {
        ListHeader {
            Text(
                stringResource(if (question is Ambiguity.DueTime) R.string.capture_question_time else R.string.capture_question_category),
                textAlign = TextAlign.Center,
            )
        }
    }
    question.answers().forEach { answer ->
        item {
            when (answer) {
                is CaptureEvent.AnswerTime -> FilledTonalButton(
                    onClick = { onEvent(answer) },
                    label = { Text(rememberDueDateLabel(answer.dueAt)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                is CaptureEvent.AnswerCategory -> {
                    val category = categories.firstOrNull { it.id == answer.categoryId }
                    FilledTonalButton(
                        onClick = { onEvent(answer) },
                        icon = category?.let { { CategoryIconDot(it) } },
                        label = { Text(category?.displayName() ?: answer.categoryId) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                else -> Unit
            }
        }
    }
    item {
        FilledTonalButton(
            onClick = { onEvent(CaptureEvent.Confirm) },
            label = { Text(stringResource(R.string.capture_save_anyway)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
