package com.flux.core.domain.usecase

import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Note
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Pending notes and reminders (active, not completed) per category id, live. */
class PendingCountsUseCase @Inject constructor(private val notes: NoteRepository) {
    operator fun invoke(): Flow<Map<String, Int>> = notes.observeNotes().map(::count)

    companion object {
        fun count(notes: List<Note>): Map<String, Int> =
            notes.filter { it.isActive && !it.completed }.groupingBy { it.categoryId }.eachCount()
    }
}
