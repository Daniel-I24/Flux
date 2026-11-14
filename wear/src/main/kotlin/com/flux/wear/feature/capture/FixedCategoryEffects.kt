package com.flux.wear.feature.capture

import com.flux.core.model.Classification

/**
 * Dictating from a category: the note goes to that category whatever the classifier says, and it
 * counts as the user's choice, so the phone's later reclassification does not move it.
 */
class FixedCategoryEffects(private val base: CaptureEffects, private val categoryId: String?) : CaptureEffects {
    override suspend fun classify(text: String): Classification {
        val classification = base.classify(text)
        return if (categoryId != null && classification.isNote) classification.inCategory(categoryId) else classification
    }

    override suspend fun save(text: String, classification: Classification, corrected: Boolean): SaveResult =
        base.save(text, classification, corrected || categoryId != null)
}
