package com.flux.wear.feature.capture

import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotANoteStateTest {
    private fun classified(isNote: Boolean) =
        CaptureEvent.Classified(Classification("tasks", "Hola", confidence = 0.9, engine = ClassifierEngine.LOCAL, isNote = isNote))

    @Test
    fun `small talk never reaches the preview and offers to try again`() {
        assertEquals(CaptureState.Error(CaptureFailure.NOT_A_NOTE), CaptureState.Processing("Hola, ¿cómo estás?").on(classified(false)))
    }

    @Test
    fun `a note goes to the preview`() {
        assertTrue(CaptureState.Processing("Comprar pan").on(classified(true)) is CaptureState.Preview)
    }
}
