package com.flux.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What a classifier returns for a dictated text. */
@Serializable
data class Classification(
    val categoryId: String,
    val title: String,
    val dueAt: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val requiresAlarm: Boolean = false,
    val confidence: Double,
    val engine: ClassifierEngine,
    /** Questions for the user before saving; empty means the note can be saved with the countdown. */
    val ambiguities: List<Ambiguity> = emptyList(),
) {
    val needsAnswer: Boolean get() = ambiguities.isNotEmpty()
}

/**
 * Something the classifier could not decide alone. The first option is the one already
 * applied to the classification, so saving without answering is still a sensible default.
 */
@Serializable
sealed interface Ambiguity {
    /** "a las 8" said at 10:00: 20:00 today or 08:00 tomorrow (epoch millis). */
    @Serializable
    @SerialName("due_time")
    data class DueTime(val options: List<Long>) : Ambiguity

    /** Two specific categories fit equally well ("comprar medicamentos": Compras o Salud). */
    @Serializable
    @SerialName("category")
    data class CategoryChoice(val options: List<String>) : Ambiguity
}
