package com.flux.wear.feature.capture

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.Text
import com.flux.core.designsystem.category.displayName
import com.flux.core.model.Ambiguity
import com.flux.core.model.Category
import com.flux.wear.R
import com.flux.wear.ui.CategoryIconDot
import com.flux.wear.ui.rememberDueDateLabel

/**
 * "¿A qué hora?", "¿En qué categoría?" or "¿Cuándo te aviso?" with one large button per option
 * (glanceable, easy to tap). A reminder also offers "Elegir fecha y hora" and "Sin recordatorio".
 */
internal fun TransformingLazyColumnScope.ambiguityQuestion(
    question: Ambiguity,
    categories: List<Category>,
    onEvent: (CaptureEvent) -> Unit,
    onPickDateTime: () -> Unit,
) {
    item { ListHeader { Text(stringResource(question.title()), textAlign = TextAlign.Center) } }
    question.answers().forEach { answer ->
        item {
            when (answer) {
                is CaptureEvent.AnswerTime -> WideButton(rememberDueDateLabel(answer.dueAt)) { onEvent(answer) }
                is CaptureEvent.AnswerReminder -> answer.dueAt?.let { dueAt ->
                    WideButton(rememberDueDateLabel(dueAt), icon = { Icon(Icons.Rounded.Alarm, contentDescription = null) }) {
                        onEvent(answer)
                    }
                }
                is CaptureEvent.AnswerCategory -> {
                    val category = categories.firstOrNull { it.id == answer.categoryId }
                    WideButton(category?.displayName() ?: answer.categoryId, icon = category?.let { { CategoryIconDot(it) } }) {
                        onEvent(answer)
                    }
                }
                else -> Unit
            }
        }
    }
    if (question is Ambiguity.ReminderTime) {
        item {
            WideButton(
                label = stringResource(R.string.capture_pick_date_time),
                icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
                onClick = onPickDateTime,
            )
        }
        item {
            WideButton(stringResource(R.string.capture_save_without_reminder), icon = {
                Icon(Icons.Rounded.NotificationsOff, contentDescription = null)
            }) {
                onEvent(CaptureEvent.AnswerReminder(null))
            }
        }
    } else {
        item { WideButton(stringResource(R.string.capture_save_anyway)) { onEvent(CaptureEvent.Confirm) } }
    }
}

@Composable
private fun WideButton(label: String, icon: (@Composable BoxScope.() -> Unit)? = null, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        icon = icon,
        label = { Text(label, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun Ambiguity.title(): Int = when (this) {
    is Ambiguity.DueTime -> R.string.capture_question_time
    is Ambiguity.CategoryChoice -> R.string.capture_question_category
    is Ambiguity.ReminderTime -> R.string.capture_question_reminder
}

/** Ambiguity options as large buttons; answering removes the question and starts the countdown. */
internal fun Ambiguity.answers(): List<CaptureEvent> = when (this) {
    is Ambiguity.DueTime -> options.map(CaptureEvent::AnswerTime)
    is Ambiguity.CategoryChoice -> options.map(CaptureEvent::AnswerCategory)
    is Ambiguity.ReminderTime -> options.map(CaptureEvent::AnswerReminder)
}
