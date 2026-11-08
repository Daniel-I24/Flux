package com.flux.core.domain.usecase

import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.NoteClassifier
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
 */
class ClassifyNoteUseCase @Inject constructor(
    private val strategies: @JvmSuppressWildcards List<NoteClassifier>,
    private val categories: CategoryRepository,
    private val profiles: ProfileRepository,
    private val clock: Clock,
    private val askReminderTime: AskReminderTimeUseCase,
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
                is FluxResult.Success -> return FluxResult.Success(askReminderTime(text, result.value, profile?.routine))
                is FluxResult.Failure -> lastError = result.error
            }
        }
        return FluxResult.Failure(lastError)
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
