package com.flux.core.domain.usecase

import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Note
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/** Notes split the way both apps show them; every list keeps its natural order. */
data class NoteSections(
    /** Pending with a future date, soonest first. */
    val upcoming: List<Note> = emptyList(),
    /** Pending without a date, newest first. */
    val pending: List<Note> = emptyList(),
    /** Pending whose date already passed, most recent first. */
    val overdue: List<Note> = emptyList(),
    /** Done, most recently updated first. */
    val completed: List<Note> = emptyList(),
) {
    val next: Note? get() = upcoming.firstOrNull()
    val isEmpty: Boolean get() = upcoming.isEmpty() && pending.isEmpty() && overdue.isEmpty() && completed.isEmpty()

    companion object {
        fun of(notes: List<Note>, now: Long): NoteSections {
            val (done, open) = notes.filter { it.isActive }.partition { it.completed }
            val (dated, undated) = open.partition { it.dueAt != null }
            val (past, future) = dated.partition { it.dueAt!! <= now }
            return NoteSections(
                upcoming = future.sortedBy { it.dueAt },
                pending = undated.sortedByDescending { it.createdAt },
                overdue = past.sortedByDescending { it.dueAt },
                completed = done.sortedByDescending { it.updatedAt },
            )
        }

        /** Next instant at which a note moves from "upcoming" to "overdue", if any. */
        fun nextChange(notes: List<Note>, now: Long): Long? =
            notes.asSequence().filter { it.isActive && !it.completed }.mapNotNull { it.dueAt }.filter { it > now }.minOrNull()
    }
}

/**
 * Real-time sections: re-evaluated when the notes change and exactly when the next reminder
 * becomes overdue (no polling), so "Próximo" and "Vencidos" never need the app to be reopened.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveNoteSectionsUseCase @Inject constructor(private val notes: NoteRepository, private val clock: Clock) {
    operator fun invoke(query: NoteQuery = NoteQuery()): Flow<NoteSections> = notes.observeNotes()
        .map { ObserveNotesUseCase.apply(it, query) }
        .flatMapLatest { list -> sectionsOverTime(list) }
        .distinctUntilChanged()

    private fun sectionsOverTime(list: List<Note>): Flow<NoteSections> = flow {
        while (true) {
            val now = clock.now()
            emit(NoteSections.of(list, now))
            // Capped so a changed system clock is noticed within a minute.
            val wait = NoteSections.nextChange(list, now)?.let { it - now + 1 } ?: MAX_WAIT_MS
            delay(wait.coerceIn(1, MAX_WAIT_MS))
        }
    }

    private companion object {
        const val MAX_WAIT_MS = 60_000L
    }
}
