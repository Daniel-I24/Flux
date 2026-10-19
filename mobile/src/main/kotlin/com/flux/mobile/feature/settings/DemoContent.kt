package com.flux.mobile.feature.settings

import android.content.Context
import com.flux.core.domain.usecase.DemoNote
import com.flux.core.model.DefaultCategory
import com.flux.core.model.Priority
import com.flux.mobile.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** The five demo examples; texts live in strings.xml. One of them rings 10 seconds after loading. */
class DemoContent @Inject constructor(@ApplicationContext private val context: Context) {
    fun examples(): List<DemoNote> = listOf(
        DemoNote(
            text = context.getString(R.string.demo_alarm_text),
            title = context.getString(R.string.demo_alarm_title),
            categoryId = DefaultCategory.HEALTH.id,
            dueInSeconds = ALARM_IN_SECONDS,
            requiresAlarm = true,
            priority = Priority.HIGH,
        ),
        DemoNote(
            text = context.getString(R.string.demo_shopping_text),
            title = context.getString(R.string.demo_shopping_title),
            categoryId = DefaultCategory.SHOPPING.id,
        ),
        DemoNote(
            text = context.getString(R.string.demo_homework_text),
            title = context.getString(R.string.demo_homework_title),
            categoryId = DefaultCategory.HOMEWORK.id,
            dueInSeconds = DAY_SECONDS * 2,
            requiresAlarm = true,
        ),
        DemoNote(
            text = context.getString(R.string.demo_reminder_text),
            title = context.getString(R.string.demo_reminder_title),
            categoryId = DefaultCategory.REMINDERS.id,
            dueInSeconds = DAY_SECONDS,
            requiresAlarm = true,
        ),
        DemoNote(
            text = context.getString(R.string.demo_idea_text),
            title = context.getString(R.string.demo_idea_title),
            categoryId = DefaultCategory.IDEAS.id,
        ),
    )

    private companion object {
        const val ALARM_IN_SECONDS = 10L
        const val DAY_SECONDS = 24 * 60 * 60L
    }
}
