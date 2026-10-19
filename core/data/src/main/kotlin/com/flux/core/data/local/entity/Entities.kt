package com.flux.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "notes", indices = [Index("dueAt"), Index("categoryId")])
data class NoteEntity(
    @PrimaryKey val id: String,
    val text: String,
    val title: String,
    val categoryId: String,
    val dueAt: Long?,
    val priority: String,
    val requiresAlarm: Boolean,
    val completed: Boolean,
    val source: String,
    val engine: String,
    val categoryCorrected: Boolean,
    /** SoundRef serialized as JSON (null = category sound or app default). */
    @ColumnInfo(defaultValue = "NULL") val soundJson: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long?,
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val defaultKey: String?,
    val customName: String?,
    val colorArgb: Long,
    val icon: String,
    /** SoundRef serialized as JSON (null = use the app default). */
    val soundJson: String?,
    val position: Int,
)

/** Watch-side offline queue: captured notes waiting for the phone (sent by WorkManager). */
@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey val noteId: String,
    val payload: ByteArray,
    val createdAt: Long,
    val attempts: Int,
) {
    override fun equals(other: Any?) = other is OutboxEntity && other.noteId == noteId && other.attempts == attempts

    override fun hashCode() = noteId.hashCode()
}
