package com.flux.core.ai.local

import com.flux.core.ai.date.SpanishDateParser
import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.model.Category
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import javax.inject.Inject

/**
 * Offline classifier: weighted Spanish keyword rules + deterministic date parser.
 * Always succeeds, so it is the last strategy of the chain.
 */
class LocalNoteClassifier @Inject constructor() : NoteClassifier {
    override val engine = ClassifierEngine.LOCAL

    override suspend fun classify(request: ClassificationRequest): FluxResult<Classification> {
        val text = request.text.trim().replace(Regex("\\s+"), " ")
        val normalized = text.normalizeSpanish()
        val now = java.time.Instant.ofEpochMilli(request.nowMillis).atZone(request.zone)
        val parsed = SpanishDateParser(request.profile?.routine).parse(text, now)
        val (categoryId, score) = pickCategory(normalized, request.categories, hasDate = parsed != null)
        val mentionsReminder = KEYWORD_RULES.first { it.category == DefaultCategory.REMINDERS }
            .patterns.any { it.containsMatchIn(normalized) }

        return FluxResult.Success(
            Classification(
                categoryId = categoryId,
                title = TitleBuilder.build(text, parsed?.fragments.orEmpty()),
                dueAt = parsed?.dateTime?.toInstant()?.toEpochMilli(),
                priority = priorityOf(normalized),
                requiresAlarm = parsed != null && (parsed.hasExplicitTime || mentionsReminder),
                confidence = if (score > 0) minOf(MAX_CONFIDENCE, BASE_CONFIDENCE + score * CONFIDENCE_STEP) else FALLBACK_CONFIDENCE,
                engine = engine,
            ),
        )
    }

    private fun pickCategory(normalized: String, categories: List<Category>, hasDate: Boolean): Pair<String, Int> {
        categories.filter { !it.isDefault && !it.customName.isNullOrBlank() }
            .firstOrNull { Regex("\\b${Regex.escape(it.customName!!.normalizeSpanish())}\\b").containsMatchIn(normalized) }
            ?.let { return it.id to CUSTOM_NAME_WEIGHT }

        val scores = KEYWORD_RULES.associate { rule ->
            rule.category to rule.weight * rule.patterns.count { it.containsMatchIn(normalized) }
        }.toMutableMap()
        if (hasDate) scores[DefaultCategory.REMINDERS] = (scores[DefaultCategory.REMINDERS] ?: 0) + DATE_REMINDER_WEIGHT

        val byScoreThenPriority = compareBy<DefaultCategory> { scores[it] ?: 0 }.thenByDescending { CATEGORY_PRIORITY.indexOf(it) }
        val best = CATEGORY_PRIORITY.maxWith(byScoreThenPriority)
        val bestScore = scores[best] ?: 0
        return if (bestScore > 0) best.id to bestScore else DefaultCategory.INFO.id to 0
    }

    private companion object {
        const val BASE_CONFIDENCE = 0.55
        const val CONFIDENCE_STEP = 0.08
        const val MAX_CONFIDENCE = 0.92
        const val FALLBACK_CONFIDENCE = 0.4
    }
}
