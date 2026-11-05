package com.flux.core.domain.usecase

import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.note
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Category filter alone and combined with "Solo pendientes" and the search, in the lists and in the sections. */
class NoteFilterTest {
    private val list = listOf(
        note("pan").copy(title = "Comprar pan", categoryId = "shopping"),
        note("leche").copy(title = "Comprar leche", categoryId = "shopping", completed = true),
        note("cita").copy(title = "Cita médica", categoryId = "health"),
        note("legacy").copy(title = "Pastillas", categoryId = "Health"),
        note("gym").copy(title = "Comprar proteína", categoryId = "cat-gym"),
        note("gym2").copy(title = "Rutina de pierna", categoryId = "cat-gym", completed = true),
    )

    private fun ids(query: NoteQuery) = ObserveNotesUseCase.apply(list, query).map { it.id }.toSet()

    @Test
    fun `filters by a default category`() {
        assertEquals(setOf("pan", "leche"), ids(NoteQuery(categoryId = "shopping")))
    }

    @Test
    fun `filters by a category the user created`() {
        assertEquals(setOf("gym", "gym2"), ids(NoteQuery(categoryId = "cat-gym")))
    }

    @Test
    fun `an id stored with other capitals still matches`() {
        assertEquals(setOf("cita", "legacy"), ids(NoteQuery(categoryId = "health")))
        assertEquals(setOf("cita", "legacy"), ids(NoteQuery(categoryId = " HEALTH ")))
    }

    @Test
    fun `category and only pending`() {
        assertEquals(setOf("pan"), ids(NoteQuery(categoryId = "shopping", includeCompleted = false)))
        assertEquals(setOf("gym"), ids(NoteQuery(categoryId = "cat-gym", includeCompleted = false)))
    }

    @Test
    fun `category and search`() {
        assertEquals(setOf("leche"), ids(NoteQuery(categoryId = "shopping", text = "LECHE")))
        assertEquals(setOf("gym"), ids(NoteQuery(categoryId = "cat-gym", text = "proteina")))
        assertTrue(ids(NoteQuery(categoryId = "health", text = "pan")).isEmpty())
    }

    @Test
    fun `category, only pending and search together`() {
        assertEquals(setOf("pan"), ids(NoteQuery(categoryId = "shopping", includeCompleted = false, text = "comprar")))
        assertTrue(ids(NoteQuery(categoryId = "shopping", includeCompleted = false, text = "leche")).isEmpty())
    }

    @Test
    fun `no category keeps every note`() {
        assertEquals(list.size, ids(NoteQuery()).size)
    }

    @Test
    fun `the sections respect the same filters`() = runTest {
        val notes = FakeNoteRepository()
        list.forEach { notes.upsert(it) }
        val observe = ObserveNoteSectionsUseCase(notes, FixedClock())
        val sections = observe(NoteQuery(categoryId = "shopping", includeCompleted = false)).first()
        assertEquals(listOf("pan"), sections.pending.map { it.id })
        assertTrue(sections.completed.isEmpty())
        val all = observe(NoteQuery(categoryId = "shopping")).first()
        assertEquals(listOf("leche"), all.completed.map { it.id })
    }
}
