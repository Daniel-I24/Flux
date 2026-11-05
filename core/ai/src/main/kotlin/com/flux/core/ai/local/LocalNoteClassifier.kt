package com.flux.core.ai.local

import com.flux.core.ai.date.SpanishDateParser
import com.flux.core.ai.text.normalizeSpaces
import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import java.time.Instant
import javax.inject.Inject

/**
 * Offline classifier: weighted Spanish keyword rules + deterministic date parser.
 * Always succeeds, so it is the last strategy of the chain.
 */
class LocalNoteClassifier @Inject constructor() : NoteClassifier {
    override val engine = ClassifierEngine.LOCAL

    override suspend fun classify(request: ClassificationRequest): FluxResult<Classification> {
        val text = request.text.normalizeSpaces()
        val normalized = text.normalizeSpanish()
        val now = Instant.ofEpochMilli(request.nowMillis).atZone(request.zone)
        val parsed = SpanishDateParser(request.profile?.routine).parse(text, now)
        val pick = CategoryScorer.pick(normalized, request.categories, hasDate = parsed != null)
        val mentionsReminder = KEYWORD_RULES.first { it.category == DefaultCategory.REMINDERS }
            .patterns.any { it.containsMatchIn(normalized) }
        val ambiguities = buildList {
            parsed?.alternatives?.takeIf { it.size > 1 }?.let { options ->
                add(Ambiguity.DueTime(options.map { it.toInstant().toEpochMilli() }))
            }
            pick.tiedWith?.let { add(Ambiguity.CategoryChoice(listOf(pick.categoryId, it))) }
        }

        return FluxResult.Success(
            Classification(
                categoryId = pick.categoryId,
                title = TitleBuilder.build(text, parsed?.fragments.orEmpty()),
                dueAt = parsed?.dateTime?.toInstant()?.toEpochMilli(),
                priority = priorityOf(normalized),
                requiresAlarm = parsed != null && (parsed.hasExplicitTime || mentionsReminder),
                confidence = confidenceOf(pick.score),
                engine = engine,
                ambiguities = ambiguities,
            ),
        )
    }

    private fun confidenceOf(score: Int): Double =
        if (score > 0) minOf(MAX_CONFIDENCE, BASE_CONFIDENCE + score * CONFIDENCE_STEP) else FALLBACK_CONFIDENCE

    private companion object {
        const val BASE_CONFIDENCE = 0.55
        const val CONFIDENCE_STEP = 0.08
        const val MAX_CONFIDENCE = 0.92
        const val FALLBACK_CONFIDENCE = 0.4
    }
}
