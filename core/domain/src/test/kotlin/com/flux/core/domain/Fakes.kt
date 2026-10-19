package com.flux.core.domain

import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.model.Category
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import com.flux.core.model.UserProfile
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FixedClock(var nowMillis: Long = 1_760_799_600_000L) : Clock {
    override fun now() = nowMillis
    override fun zone(): ZoneId = ZoneId.of("America/Bogota")
}

class SequentialIds : IdGenerator {
    private val next = AtomicInteger()
    override fun newId() = "id-${next.incrementAndGet()}"
}

class FakeAlarms : AlarmScheduler {
    val scheduled = mutableMapOf<String, Long?>()
    val cancelled = mutableListOf<String>()
    override fun schedule(note: Note) {
        scheduled[note.id] = note.dueAt
    }
    override fun cancel(noteId: String) {
        scheduled.remove(noteId)
        cancelled += noteId
    }
}

class FakeNoteRepository : NoteRepository {
    val store = MutableStateFlow<Map<String, Note>>(emptyMap())
    override fun observeNotes(): Flow<List<Note>> = store.map { map -> map.values.filter { it.isActive } }
    override fun observeNote(id: String): Flow<Note?> = store.map { it[id] }
    override suspend fun getNote(id: String) = store.value[id]
    override fun observeUpcoming(fromMillis: Long, limit: Int): Flow<List<Note>> = store.map { map ->
        map.values.filter { it.isActive && !it.completed && (it.dueAt ?: -1) >= fromMillis }.sortedBy { it.dueAt }.take(limit)
    }
    override suspend fun getPendingAlarms() = store.value.values.filter { it.hasPendingAlarm }
    override suspend fun upsert(note: Note) {
        store.value = store.value + (note.id to note)
    }
    override suspend fun upsertIfNewer(note: Note): Boolean {
        val current = store.value[note.id]
        if (current != null && current.updatedAt >= note.updatedAt) return false
        upsert(note)
        return true
    }
    override suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String, updatedAt: Long) {
        store.value = store.value.mapValues { (_, stored) ->
            val moves = stored.categoryId == fromCategoryId
            if (moves) stored.copy(categoryId = toCategoryId, updatedAt = updatedAt) else stored
        }
    }
    override suspend fun clear() {
        store.value = emptyMap()
    }
}

class FakeCategoryRepository(initial: List<Category> = DefaultCategory.all()) : CategoryRepository {
    val store = MutableStateFlow(initial)
    override fun observeCategories() = store
    override suspend fun getCategories() = store.value
    override suspend fun upsert(category: Category) {
        store.value = store.value.filterNot { it.id == category.id } + category
    }
    override suspend fun delete(id: String) {
        store.value = store.value.filterNot { it.id == id }
    }
    override suspend fun ensureDefaults() = Unit
}

class FakeProfileRepository(var profile: UserProfile? = null) : ProfileRepository {
    override fun observeProfile() = MutableStateFlow(profile)
    override suspend fun getProfile() = profile
    override suspend fun saveProfile(profile: UserProfile) {
        this.profile = profile
    }
    override suspend fun clear() {
        profile = null
    }
}

fun note(id: String, updatedAt: Long = 1L, dueAt: Long? = null, alarm: Boolean = false, categoryId: String = "tasks") = Note(
    id = id,
    text = "Texto $id",
    title = "Nota $id",
    categoryId = categoryId,
    dueAt = dueAt,
    requiresAlarm = alarm,
    source = DeviceSource.WATCH,
    engine = ClassifierEngine.LOCAL,
    createdAt = 1L,
    updatedAt = updatedAt,
)
