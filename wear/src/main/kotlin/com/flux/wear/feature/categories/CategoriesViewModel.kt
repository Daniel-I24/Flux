package com.flux.wear.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.usecase.PendingCountsUseCase
import com.flux.core.model.Category
import com.flux.core.model.DefaultCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** A category and how many notes or reminders are still pending in it (agenda cached from the phone). */
data class CategoryCount(val category: Category, val pending: Int)

@HiltViewModel
class CategoriesViewModel @Inject constructor(repository: CategoryRepository, pendingCounts: PendingCountsUseCase) : ViewModel() {
    val categories: StateFlow<List<CategoryCount>> = combine(repository.observeCategories(), pendingCounts()) { categories, counts ->
        categories.ifEmpty { DefaultCategory.all() }.map { CategoryCount(it, counts[it.id] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
