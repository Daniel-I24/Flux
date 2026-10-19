package com.flux.core.data.local

import com.flux.core.data.local.entity.CategoryEntity
import com.flux.core.data.local.entity.NoteEntity
import com.flux.core.model.Category
import com.flux.core.model.CategoryIcon
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import com.flux.core.model.Priority
import com.flux.core.model.SoundRef
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

/** Unknown enum values (older/newer data) degrade to safe defaults instead of crashing. */
private inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T): T =
    enumValues<T>().firstOrNull { it.name == value } ?: fallback

private fun decodeSound(value: String): SoundRef? = runCatching { json.decodeFromString(SoundRef.serializer(), value) }.getOrNull()

private fun encodeSound(sound: SoundRef): String = json.encodeToString(SoundRef.serializer(), sound)

fun NoteEntity.toModel() = Note(
    id = id,
    text = text,
    title = title,
    categoryId = categoryId,
    dueAt = dueAt,
    priority = enumOr(priority, Priority.NORMAL),
    requiresAlarm = requiresAlarm,
    completed = completed,
    source = enumOr(source, DeviceSource.PHONE),
    engine = enumOr(engine, ClassifierEngine.LOCAL),
    categoryCorrected = categoryCorrected,
    sound = soundJson?.let(::decodeSound),
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun Note.toEntity() = NoteEntity(
    id = id,
    text = text,
    title = title,
    categoryId = categoryId,
    dueAt = dueAt,
    priority = priority.name,
    requiresAlarm = requiresAlarm,
    completed = completed,
    source = source.name,
    engine = engine.name,
    categoryCorrected = categoryCorrected,
    soundJson = sound?.let(::encodeSound),
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
)

fun CategoryEntity.toModel() = Category(
    id = id,
    defaultKey = defaultKey?.let { key -> DefaultCategory.entries.firstOrNull { it.name == key } },
    customName = customName,
    colorArgb = colorArgb,
    icon = enumOr(icon, CategoryIcon.STAR),
    sound = soundJson?.let(::decodeSound),
    position = position,
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    defaultKey = defaultKey?.name,
    customName = customName,
    colorArgb = colorArgb,
    icon = icon.name,
    soundJson = sound?.let(::encodeSound),
    position = position,
)
