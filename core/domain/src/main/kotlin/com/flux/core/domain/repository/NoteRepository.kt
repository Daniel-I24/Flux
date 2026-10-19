package com.flux.core.domain.repository

import com.flux.core.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    /** Active (not deleted) notes. */
    fun observeNotes(): Flow<List<Note>>

    fun observeNote(id: String): Flow<Note?>

    suspend fun getNote(id: String): Note?

    /** Active, not completed notes with a due date in [fromMillis, ∞), soonest first. */
    fun observeUpcoming(fromMillis: Long, limit: Int): Flow<List<Note>>

    suspend fun getPendingAlarms(): List<Note>

    suspend fun upsert(note: Note)

    /** Last-write-wins merge. Returns true when [note] replaced the stored revision. */
    suspend fun upsertIfNewer(note: Note): Boolean

    suspend fun reassignCategory(fromCategoryId: String, toCategoryId: String, updatedAt: Long)

    suspend fun clear()
}
