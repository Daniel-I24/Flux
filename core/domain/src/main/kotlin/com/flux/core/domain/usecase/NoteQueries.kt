package com.flux.core.domain.usecase

import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.Normalizer
import javax.inject.Inject

enum class NoteSort { NEWEST, DUE_DATE, CATEGORY }

data class NoteQuery(
    val text: String = "",
    val categoryId: String? = null,
    val sort: NoteSort = NoteSort.NEWEST,
    val includeCompleted: Boolean = true,
)

/** Search (accent-insensitive), filter by category and sort; pending notes always first. */
class ObserveNotesUseCase @Inject constructor(private val notes: NoteRepository) {
    operator fun invoke(query: NoteQuery): Flow<List<Note>> = notes.observeNotes().map { list -> apply(list, query) }

    companion object {
        fun apply(list: List<Note>, query: NoteQuery): List<Note> {
            val needle = query.text.normalizedForSearch()
            val filtered = list.filter { note ->
                (query.categoryId == null || note.categoryId == query.categoryId) &&
                    (query.includeCompleted || !note.completed) &&
                    (needle.isEmpty() || "${note.title} ${note.text}".normalizedForSearch().contains(needle))
            }
            val order: Comparator<Note> = when (query.sort) {
                NoteSort.NEWEST -> compareByDescending { it.createdAt }
                NoteSort.DUE_DATE -> compareBy<Note, Long?>(nullsLast()) { it.dueAt }.thenByDescending { it.createdAt }
                NoteSort.CATEGORY -> compareBy<Note> { it.categoryId }.thenByDescending { it.createdAt }
            }
            return filtered.sortedWith(compareBy<Note> { it.completed }.then(order))
        }
    }
}

class ObserveUpcomingRemindersUseCase @Inject constructor(private val notes: NoteRepository) {
    operator fun invoke(fromMillis: Long, limit: Int = DEFAULT_LIMIT): Flow<List<Note>> = notes.observeUpcoming(fromMillis, limit)

    private companion object {
        const val DEFAULT_LIMIT = 10
    }
}

/** Counts per category id for the home summary. */
class ObserveCategorySummaryUseCase @Inject constructor(private val notes: NoteRepository) {
    operator fun invoke(): Flow<Map<String, Int>> =
        notes.observeNotes().map { list -> list.filter { !it.completed }.groupingBy { it.categoryId }.eachCount() }
}

private val DIACRITICS = Regex("\\p{Mn}+")

fun String.normalizedForSearch(): String =
    Normalizer.normalize(trim().lowercase(), Normalizer.Form.NFD).replace(DIACRITICS, "")
