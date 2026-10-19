package com.flux.mobile.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.CategoryDraft
import com.flux.core.domain.usecase.DeleteCategoryUseCase
import com.flux.core.domain.usecase.ObserveCategoriesUseCase
import com.flux.core.domain.usecase.ObserveCategorySummaryUseCase
import com.flux.core.domain.usecase.SaveCategoryUseCase
import com.flux.core.model.FluxResult
import com.flux.mobile.R
import com.flux.mobile.feature.home.CategoryCount
import com.flux.mobile.ui.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoriesUiState(val items: List<CategoryCount> = emptyList(), val loading: Boolean = true)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    observeCategories: ObserveCategoriesUseCase,
    observeSummary: ObserveCategorySummaryUseCase,
    private val saveCategory: SaveCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
) : ViewModel() {
    private val messages = MutableStateFlow<Int?>(null)

    /** One-shot message for the snackbar. */
    val message: StateFlow<Int?> = messages.asStateFlow()

    val uiState: StateFlow<CategoriesUiState> = combine(observeCategories(), observeSummary()) { categories, summary ->
        CategoriesUiState(categories.map { CategoryCount(it, summary[it.id] ?: 0) }, loading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CategoriesUiState())

    /** [defaultNames] are the localized names of built-in categories, used to reject duplicates. */
    fun save(draft: CategoryDraft, defaultNames: Map<String, String>, onDone: () -> Unit) {
        viewModelScope.launch {
            when (saveCategory(draft, defaultNames)) {
                is FluxResult.Success -> onDone()
                is FluxResult.Failure -> messages.value = R.string.categories_name_invalid
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            messages.value = when (val result = deleteCategory(id)) {
                is FluxResult.Success -> R.string.categories_deleted
                is FluxResult.Failure -> result.error.messageRes()
            }
        }
    }

    fun consumeMessage() {
        messages.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
