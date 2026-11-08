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

    /**
     * A reminder whose time or day was left incomplete ("a las", "mañana a las", "el viernes").
     * Unlike the other questions nothing is applied in advance: the note has no date until the
     * user picks a suggestion (epoch millis), another date and time, or "Guardar sin recordatorio".
     */
    @Serializable
    @SerialName("reminder_time")
    data class ReminderTime(val options: List<Long>) : Ambiguity
}
