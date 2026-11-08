package com.flux.core.domain.usecase

import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import javax.inject.Inject

/**
 * The user's answer to one ambiguity. Shared by the phone (chips) and the watch (large buttons)
 * so both apply exactly the same rule; answering removes the question from the classification.
 */
class ResolveAmbiguityUseCase @Inject constructor() {
    fun chooseTime(classification: Classification, dueAt: Long): Classification {
        val question = classification.ambiguities.filterIsInstance<Ambiguity.DueTime>().firstOrNull() ?: return classification
        if (dueAt !in question.options) return classification
        return classification.copy(dueAt = dueAt, ambiguities = classification.ambiguities - question)
    }

    fun chooseCategory(classification: Classification, categoryId: String): Classification {
        val question = classification.ambiguities.filterIsInstance<Ambiguity.CategoryChoice>().firstOrNull() ?: return classification
        if (categoryId !in question.options) return classification
        return classification.copy(categoryId = categoryId, ambiguities = classification.ambiguities - question)
    }

    /**
     * "¿Cuándo te aviso?": a suggestion or any date picked by the user turns the alarm on;
     * null is "Guardar sin recordatorio" (no date, no alarm).
     */
    fun chooseReminder(classification: Classification, dueAt: Long?): Classification {
        val question = classification.ambiguities.filterIsInstance<Ambiguity.ReminderTime>().firstOrNull() ?: return classification
        return classification.copy(dueAt = dueAt, requiresAlarm = dueAt != null, ambiguities = classification.ambiguities - question)
    }

    /** "Guardar igual": keeps the first option of every question (already applied); an open reminder stays without one. */
    fun acceptDefaults(classification: Classification): Classification = classification.copy(ambiguities = emptyList())
}
