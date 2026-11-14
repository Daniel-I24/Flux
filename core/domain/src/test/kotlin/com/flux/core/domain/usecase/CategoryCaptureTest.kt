package com.flux.core.domain.usecase

import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryCaptureTest {
    private fun note(id: String, category: String, completed: Boolean = false, deleted: Boolean = false) = Note(
        id = id, text = id, title = id, categoryId = category, completed = completed, deletedAt = if (deleted) 1 else null,
        source = DeviceSource.PHONE, engine = ClassifierEngine.LOCAL, createdAt = 0, updatedAt = 0,
    )

    @Test
    fun `each category counts only its pending notes`() {
        val counts = PendingCountsUseCase.count(
            listOf(
                note("a", "shopping"),
                note("b", "shopping"),
                note("c", "shopping", completed = true),
                note("d", "tasks", deleted = true)
            ),
        )
        assertEquals(mapOf("shopping" to 2), counts)
    }

    @Test
    fun `a note created inside a category stays there and asks no category question`() {
        val classification = Classification(
            categoryId = "health",
            title = "Medicamentos",
            confidence = 0.6,
            engine = ClassifierEngine.LOCAL,
            ambiguities = listOf(Ambiguity.CategoryChoice(listOf("health", "shopping")), Ambiguity.DueTime(listOf(1L, 2L))),
        )
        val fixed = classification.inCategory("shopping")
        assertEquals("shopping", fixed.categoryId)
        assertTrue(fixed.ambiguities.none { it is Ambiguity.CategoryChoice })
        assertEquals(1, fixed.ambiguities.size)
    }
}
