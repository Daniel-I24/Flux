package com.flux.wear.sync

import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MergeAgendaTest {
    private fun note(id: String, updatedAt: Long, completed: Boolean = false) = Note(
        id = id,
        text = "Texto",
        title = "Nota",
        categoryId = "tasks",
        completed = completed,
        source = DeviceSource.WATCH,
        engine = ClassifierEngine.LOCAL,
        createdAt = 1,
        updatedAt = updatedAt,
    )

    @Test
    fun `a capture still in the queue stays until the phone has it`() {
        val merged = mergeAgenda(listOf(note("phone", 5)), listOf(note("queued", 6)))
        assertEquals(listOf("phone", "queued"), merged.map { it.id })
    }

    @Test
    fun `a change made on the watch is not undone by an older snapshot`() {
        val merged = mergeAgenda(listOf(note("a", 5)), listOf(note("a", 9, completed = true)))
        assertEquals(1, merged.size)
        assertTrue(merged.single().completed)
    }

    @Test
    fun `the phone wins once it has the same or a newer revision`() {
        assertEquals(10L, mergeAgenda(listOf(note("a", 10)), listOf(note("a", 9))).single().updatedAt)
        assertEquals(9L, mergeAgenda(listOf(note("a", 9)), listOf(note("a", 9))).single().updatedAt)
    }
}
