package com.flux.core.data.repository

import androidx.room.withTransaction
import com.flux.core.data.local.FluxDatabase
import com.flux.core.data.local.toEntity
import com.flux.core.data.local.toModel
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Note
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomNoteRepository @Inject constructor(private val database: FluxDatabase) : NoteRepository {
    private val dao = database.notes()

    override fun observeNotes(): Flow<List<Note>> = dao.observeActive().map { list -> list.map { it.toModel() } }.distinctUntilChanged()

    override fun observeNote(id: String): Flow<Note?> = dao.observe(id).map { it?.toModel() }.distinctUntilChanged()

    override suspend fun getNote(id: String): Note? = dao.get(id)?.toModel()

    override fun observeUpcoming(fromMillis: Long, limit: Int): Flow<List<Note>> =
        dao.observeUpcoming(fromMillis, limit).map { list -> list.map { it.toModel() } }.distinctUntilChanged()

    override suspend fun getPendingAlarms(): List<Note> = dao.pendingAlarms().map { it.toModel() }

    override suspend fun upsert(note: Note) = dao.upsert(note.toEntity())

    /** Atomic read-compare-write so concurrent deliveries cannot regress a note. */
    override suspend fun upsertIfNewer(note: Note): Boolean = database.withTransaction {
        val current = dao.get(note.id)
        if (current != null && current.updatedAt >= note.updatedAt) {
            false
        } else {
            dao.upsert(note.toEntity())
            true
        }
    }

    override suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String, updatedAt: Long) =
        dao.reassign(fromCategoryId, toCategoryId, updatedAt)

    override suspend fun clear() = dao.clear()
}
