package com.flux.core.domain.usecase

import com.flux.core.domain.FakeCategoryRepository
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.SequentialIds
import com.flux.core.domain.note
import com.flux.core.model.AppError
import com.flux.core.model.CategoryIcon
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QueriesAndCategoriesTest {
    @Test
    fun `search is accent insensitive, filters and keeps pending first`() {
        val list = listOf(
            note("a").copy(title = "Comprar café", createdAt = 1),
            note("b").copy(title = "Idea de batería", categoryId = "ideas", createdAt = 3, completed = true),
            note("c").copy(title = "Código 4829", categoryId = "info", createdAt = 2),
        )
        assertEquals(listOf("c", "a", "b"), ObserveNotesUseCase.apply(list, NoteQuery()).map { it.id })
        assertEquals(listOf("a"), ObserveNotesUseCase.apply(list, NoteQuery(text = "CAFE")).map { it.id })
        assertEquals(listOf("b"), ObserveNotesUseCase.apply(list, NoteQuery(categoryId = "ideas")).map { it.id })
        assertEquals(2, ObserveNotesUseCase.apply(list, NoteQuery(includeCompleted = false)).size)
    }

    @Test
    fun `sort by due date puts undated notes last`() {
        val list = listOf(note("a", dueAt = null), note("b", dueAt = 20), note("c", dueAt = 10))
        assertEquals(listOf("c", "b", "a"), ObserveNotesUseCase.apply(list, NoteQuery(sort = NoteSort.DUE_DATE)).map { it.id })
    }

    @Test
    fun `saving a category validates and rejects duplicates`() = runTest {
        val categories = FakeCategoryRepository()
        val save = SaveCategoryUseCase(categories, SequentialIds())
        val names = mapOf("tasks" to "Tareas")
        assertEquals(FluxResult.Failure(AppError.InvalidInput), save(CategoryDraft(name = " ", colorArgb = 0, icon = CategoryIcon.STAR)))
        assertEquals(
            FluxResult.Failure(AppError.InvalidInput),
            save(CategoryDraft(name = "tareas", colorArgb = 0, icon = CategoryIcon.STAR), names)
        )
        val created = save(CategoryDraft(name = "Gimnasio", colorArgb = 1, icon = CategoryIcon.SPORT)) as FluxResult.Success
        assertEquals("Gimnasio", created.value.customName)
        assertEquals(DefaultCategory.entries.size, created.value.position)
    }

    @Test
    fun `renaming a default keeps its key`() = runTest {
        val categories = FakeCategoryRepository()
        val result = SaveCategoryUseCase(categories, SequentialIds())(
            CategoryDraft(id = "tasks", name = "Pendientes", colorArgb = 2, icon = CategoryIcon.TASK),
        ) as FluxResult.Success
        assertEquals(DefaultCategory.TASKS, result.value.defaultKey)
        assertEquals("Pendientes", result.value.customName)
    }

    @Test
    fun `only custom categories can be deleted and their notes move to info`() = runTest {
        val categories = FakeCategoryRepository()
        val notes = FakeNoteRepository()
        val created = (
            SaveCategoryUseCase(
                categories,
                SequentialIds()
            )(CategoryDraft(name = "Gym", colorArgb = 1, icon = CategoryIcon.SPORT)) as FluxResult.Success
            ).value
        notes.upsert(note("n", categoryId = created.id))
        val delete = DeleteCategoryUseCase(categories, notes, FixedClock())
        assertEquals(FluxResult.Failure(AppError.InvalidInput), delete("tasks"))
        assertEquals(FluxResult.Success(Unit), delete(created.id))
        assertEquals(DefaultCategory.INFO.id, notes.getNote("n")!!.categoryId)
        assertTrue(categories.store.value.none { it.id == created.id })
    }
}
