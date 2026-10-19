package com.flux.core.ai.local

import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.model.Note

/**
 * Short title: removes spoken fillers ("recuérdame que…") and the date phrase,
 * keeps the user's original casing and accents. Input must be single-spaced.
 */
internal object TitleBuilder {
    private const val TITLE_LENGTH = 60
    private const val ELLIPSIS = "…"

    fun build(text: String, dateFragments: List<String>): String {
        var title = stripFillers(text)
        dateFragments.forEach { fragment ->
            val index = title.normalizeSpanish().indexOf(fragment)
            if (index >= 0) title = title.removeRange(index, index + fragment.length)
        }
        val cleaned = title
            .replace(Regex("\\s+"), " ")
            .replace(Regex("[\\s,.;:]+$"), "")
            .let { it.replace(TRAILING_CONNECTORS, "").ifEmpty { it } }
            .trim()
            .ifEmpty { text }
        return truncate(cleaned.replaceFirstChar { it.uppercase() })
    }

    private fun stripFillers(text: String): String {
        var start = 0
        SPOKEN_FILLERS.forEach { filler ->
            val remaining = text.substring(start)
            val match = filler.find(remaining.normalizeSpanish()) ?: return@forEach
            start += match.value.length
        }
        return text.substring(start.coerceAtMost(text.length))
    }

    private fun truncate(value: String): String {
        val limit = minOf(TITLE_LENGTH, Note.MAX_TITLE_LENGTH)
        return if (value.length <= limit) value else value.take(limit - 1).trimEnd() + ELLIPSIS
    }
}
