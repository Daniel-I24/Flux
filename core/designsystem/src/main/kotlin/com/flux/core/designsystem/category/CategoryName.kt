package com.flux.core.designsystem.category

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import com.flux.core.designsystem.R
import com.flux.core.model.Category
import com.flux.core.model.DefaultCategory

@StringRes
fun DefaultCategory.nameRes(): Int = when (this) {
    DefaultCategory.TASKS -> R.string.category_tasks
    DefaultCategory.HOMEWORK -> R.string.category_homework
    DefaultCategory.IDEAS -> R.string.category_ideas
    DefaultCategory.MEALS -> R.string.category_meals
    DefaultCategory.HEALTH -> R.string.category_health
    DefaultCategory.SHOPPING -> R.string.category_shopping
    DefaultCategory.REMINDERS -> R.string.category_reminders
    DefaultCategory.INFO -> R.string.category_info
}

/** Localized name: the user's custom name wins, defaults come from strings.xml. */
@Composable
@ReadOnlyComposable
fun Category.displayName(): String =
    customName ?: defaultKey?.let { stringResource(it.nameRes()) } ?: stringResource(R.string.category_unnamed)
