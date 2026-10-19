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
) {
    suspend operator fun invoke(rawText: String): FluxResult<Classification> {
        val text = rawText.trim().replace(WHITESPACE, " ").take(Note.MAX_TEXT_LENGTH)
        if (text.isEmpty()) return FluxResult.Failure(AppError.InvalidInput)
        val request = ClassificationRequest(
            text = text,
            nowMillis = clock.now(),
            zone = clock.zone(),
            categories = categories.getCategories(),
            profile = profiles.getProfile(),
        )
        var lastError: AppError = AppError.Unexpected("No classifier available")
        for (strategy in strategies) {
            when (val result = strategy.classify(request)) {
                is FluxResult.Success -> return result
                is FluxResult.Failure -> lastError = result.error
            }
        }
        return FluxResult.Failure(lastError)
    }

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}
