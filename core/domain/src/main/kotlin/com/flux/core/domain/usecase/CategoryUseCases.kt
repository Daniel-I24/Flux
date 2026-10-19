package com.flux.core.domain.usecase

import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.AppError
import com.flux.core.model.Category
import com.flux.core.model.CategoryIcon
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import com.flux.core.model.SoundRef
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveCategoriesUseCase @Inject constructor(private val categories: CategoryRepository) {
    operator fun invoke(): Flow<List<Category>> = categories.observeCategories().map { list -> list.sortedBy { it.position } }
}

data class CategoryDraft(
    val id: String? = null,
    val name: String,
    val colorArgb: Long,
    val icon: CategoryIcon,
    val sound: SoundRef? = null,
)

/** Creates or edits a category; names are required, short and unique (case-insensitive). */
class SaveCategoryUseCase @Inject constructor(
    private val categories: CategoryRepository,
    private val ids: IdGenerator,
) {
    suspend operator fun invoke(draft: CategoryDraft, defaultNames: Map<String, String> = emptyMap()): FluxResult<Category> {
        val name = draft.name.trim()
        if (name.isEmpty() || name.length > Category.MAX_NAME_LENGTH) return FluxResult.Failure(AppError.InvalidInput)
        val existing = categories.getCategories()
        val duplicated = existing.any { other ->
            other.id != draft.id && (other.customName ?: defaultNames[other.id]).equals(name, ignoreCase = true)
        }
        if (duplicated) return FluxResult.Failure(AppError.InvalidInput)
        val current = existing.firstOrNull { it.id == draft.id }
        val category = Category(
            id = current?.id ?: ids.newId(),
            defaultKey = current?.defaultKey,
            customName = name.takeUnless { current?.defaultKey != null && defaultNames[current.id] == it },
            colorArgb = draft.colorArgb,
            icon = draft.icon,
            sound = draft.sound,
            position = current?.position ?: (existing.maxOfOrNull { it.position } ?: -1) + 1,
        )
        categories.upsert(category)
        return FluxResult.Success(category)
    }
}

/** Only user categories can be deleted; their notes move to "Información". */
class DeleteCategoryUseCase @Inject constructor(
    private val categories: CategoryRepository,
    private val notes: NoteRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: String): FluxResult<Unit> {
        val category = categories.getCategories().firstOrNull { it.id == id } ?: return FluxResult.Failure(AppError.NotFound)
        if (category.isDefault) return FluxResult.Failure(AppError.InvalidInput)
        notes.reassignCategory(id, DefaultCategory.INFO.id, clock.now())
        categories.delete(id)
        return FluxResult.Success(Unit)
    }
}
