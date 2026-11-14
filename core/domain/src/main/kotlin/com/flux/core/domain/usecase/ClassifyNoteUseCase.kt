package com.flux.core.domain.usecase

import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.domain.port.NoteIntentDetector
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.model.AppError
import com.flux.core.model.Classification
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import javax.inject.Inject

/**
 * Strategy context: tries each classifier in priority order (remote first, local last)
 * and falls back automatically. The local strategy never fails, so the user is never blocked.
 * A text that is not a note (for the AI or for [NoteIntentDetector]) comes back with isNote = false.
 */
class ClassifyNoteUseCase @Inject constructor(
    private val strategies: @JvmSuppressWildcards List<NoteClassifier>,
    private val categories: CategoryRepository,
    private val profiles: ProfileRepository,
    private val clock: Clock,
    private val askReminderTime: AskReminderTimeUseCase,
    private val intent: NoteIntentDetector,
) {
    suspend operator fun invoke(rawText: String): FluxResult<Classification> {
        val text = rawText.trim().replace(WHITESPACE, " ").take(Note.MAX_TEXT_LENGTH)
        if (text.isEmpty()) return FluxResult.Failure(AppError.InvalidInput)
        val profile = profiles.getProfile()
        val request = ClassificationRequest(
            text = text,
            nowMillis = clock.now(),
            zone = clock.zone(),
            categories = categories.getCategories(),
            profile = profile,
        )
        var lastError: AppError = AppError.Unexpected("No classifier available")
        for (strategy in strategies) {
            when (val result = strategy.classify(request)) {
                is FluxResult.Success -> return FluxResult.Success(
                    if (result.value.isNote && intent.isNote(text)) {
                        askReminderTime(text, result.value, profile?.routine)
                    } else {
                        // Small talk or noise: the UI asks to try again and nothing is saved.
                        result.value.copy(isNote = false, dueAt = null, requiresAlarm = false, ambiguities = emptyList())
                    },
                )
                is FluxResult.Failure -> lastError = result.error
            }
        }
        return FluxResult.Failure(lastError)
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
