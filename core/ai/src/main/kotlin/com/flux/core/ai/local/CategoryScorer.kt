package com.flux.core.ai.local

import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.model.Category
import com.flux.core.model.DefaultCategory

/** Winner of the keyword scoring and, when two specific categories tie, the runner-up. */
internal data class CategoryPick(val categoryId: String, val score: Int, val tiedWith: String? = null)

/**
 * Weighted keyword scoring. Business rule (N-053): an action with a date is a Task (or a more
 * specific category such as Compras or Salud); a Reminder is a notice without a concrete action.
 */
internal object CategoryScorer {
    /** Categories specific enough that a tie between two of them deserves a question. */
    private val SPECIFIC = setOf(DefaultCategory.HOMEWORK, DefaultCategory.SHOPPING, DefaultCategory.HEALTH, DefaultCategory.MEALS)

    fun pick(normalized: String, categories: List<Category>, hasDate: Boolean): CategoryPick {
        customByName(normalized, categories)?.let { return CategoryPick(it.id, CUSTOM_NAME_WEIGHT) }

        val scores = KEYWORD_RULES.associate { rule ->
            rule.category to rule.weight * rule.patterns.count { it.containsMatchIn(normalized) }
        }.toMutableMap()
        val hasAction = (scores[DefaultCategory.TASKS] ?: 0) > 0
        when {
            hasAction -> scores[DefaultCategory.REMINDERS] = 0
            hasDate -> scores[DefaultCategory.REMINDERS] = (scores[DefaultCategory.REMINDERS] ?: 0) + DATE_REMINDER_WEIGHT
        }

        val ranking = CATEGORY_PRIORITY
            .filter { (scores[it] ?: 0) > 0 }
            .sortedWith(compareByDescending<DefaultCategory> { scores[it] ?: 0 }.thenBy { CATEGORY_PRIORITY.indexOf(it) })
        val best = ranking.firstOrNull() ?: return CategoryPick(available(DefaultCategory.INFO, categories), 0)
        val bestScore = scores.getValue(best)
        val runnerUp = ranking.getOrNull(1)?.takeIf { scores[it] == bestScore && it in SPECIFIC && best in SPECIFIC }
        return CategoryPick(available(best, categories), bestScore, runnerUp?.let { available(it, categories) })
    }

    private fun customByName(normalized: String, categories: List<Category>): Category? =
        categories.filter { !it.isDefault && !it.customName.isNullOrBlank() }
            .firstOrNull { Regex("\\b${Regex.escape(it.customName!!.normalizeSpanish())}\\b").containsMatchIn(normalized) }

    /** The id the user's category list really has (a default renamed by the user keeps its id). */
    private fun available(category: DefaultCategory, categories: List<Category>): String =
        categories.firstOrNull { it.id == category.id || it.defaultKey == category }?.id
            ?: categories.firstOrNull { it.defaultKey == DefaultCategory.INFO }?.id
            ?: category.id
}
