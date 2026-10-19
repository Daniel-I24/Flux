package com.flux.core.model

import kotlinx.serialization.Serializable

/**
 * A captured note. Deletion is soft ([deletedAt]) so it can be undone and synced;
 * [updatedAt] drives last-write-wins between phone and watch.
 */
@Serializable
data class Note(
    val id: String,
    val text: String,
    val title: String,
    val categoryId: String,
    val dueAt: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val requiresAlarm: Boolean = false,
    val completed: Boolean = false,
    val source: DeviceSource,
    val engine: ClassifierEngine,
    val categoryCorrected: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
) {
    val isActive: Boolean get() = deletedAt == null
    val hasPendingAlarm: Boolean get() = isActive && !completed && requiresAlarm && dueAt != null

    companion object {
        const val MAX_TEXT_LENGTH = 500
        const val MAX_TITLE_LENGTH = 80
    }
}

@Serializable
enum class Priority { LOW, NORMAL, HIGH }

@Serializable
enum class DeviceSource { WATCH, PHONE }

@Serializable
enum class ClassifierEngine { REMOTE, LOCAL }
