package com.flux.core.domain.usecase

import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.model.AppError
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.FluxResult
import javax.inject.Inject
import javax.inject.Qualifier

/** Marks the remote (Gemini) strategy when it must be used alone. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RemoteClassifier

enum class ReclassifyOutcome { UPDATED, SKIPPED, RETRY_LATER }

/**
 * A note classified offline (phone without internet, server down) is classified
 * again with the remote AI once it is reachable. User corrections are never overwritten.
 */
class ReclassifyNoteUseCase @Inject constructor(
    private val notes: NoteRepository,
    private val categories: CategoryRepository,
    private val profiles: ProfileRepository,
    private val update: UpdateNoteUseCase,
    private val clock: Clock,
    @RemoteClassifier private val remote: NoteClassifier,
) {
    suspend operator fun invoke(noteId: String): ReclassifyOutcome {
        val note = notes.getNote(noteId) ?: return ReclassifyOutcome.SKIPPED
        if (!note.isActive || note.categoryCorrected || note.engine == ClassifierEngine.REMOTE) return ReclassifyOutcome.SKIPPED
        val request = ClassificationRequest(note.text, note.createdAt, clock.zone(), categories.getCategories(), profiles.getProfile())
        return when (val result = remote.classify(request)) {
            is FluxResult.Success -> {
                val better = result.value
                update(noteId) { current ->
                    if (current.categoryCorrected) {
                        current
                    } else {
                        current.copy(
                            categoryId = better.categoryId,
                            title = better.title,
                            priority = better.priority,
                            dueAt = current.dueAt ?: better.dueAt,
                            requiresAlarm = current.requiresAlarm || better.requiresAlarm,
                            engine = ClassifierEngine.REMOTE,
                        )
                    }
                }
                ReclassifyOutcome.UPDATED
            }
            is FluxResult.Failure -> if (result.error.isTransient()) ReclassifyOutcome.RETRY_LATER else ReclassifyOutcome.SKIPPED
        }
    }

    private fun AppError.isTransient() = this == AppError.Network || this == AppError.Timeout || this == AppError.ServerUnavailable
}
