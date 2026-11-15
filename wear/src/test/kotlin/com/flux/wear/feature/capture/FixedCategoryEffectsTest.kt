package com.flux.wear.feature.capture

import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FixedCategoryEffectsTest {
    private var savedCorrected: Boolean? = null
    private fun base(isNote: Boolean = true) = object : CaptureEffects {
        override suspend fun classify(text: String) =
            Classification("tasks", "Pan", confidence = 0.9, engine = ClassifierEngine.LOCAL, isNote = isNote)

        override suspend fun save(text: String, classification: Classification, corrected: Boolean, alarmLeadMinutes: Int?): SaveResult {
            savedCorrected = corrected
            return SaveResult(note = null, queued = false)
        }
    }

    @Test
    fun `dictating from a category saves the note there as the user's choice`() = runTest {
        val effects = FixedCategoryEffects(base(), categoryId = "shopping")
        val classification = effects.classify("comprar pan")
        assertEquals("shopping", classification.categoryId)
        effects.save("comprar pan", classification, corrected = false, alarmLeadMinutes = null)
        assertTrue(savedCorrected == true)
    }

    @Test
    fun `without a category the classifier decides`() = runTest {
        assertEquals("tasks", FixedCategoryEffects(base(), categoryId = null).classify("x").categoryId)
    }
}
