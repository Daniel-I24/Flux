package com.flux.core.model

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
)
