package com.flux.core.ai.local

import com.flux.core.ai.text.foldAccentsKeepingLength
import com.flux.core.ai.text.normalizeSpaces
import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.model.Note

/** A title must say something: at least two letters and not a placeholder such as "Nota sin título". */
object TitleRules {
    private const val MIN_LETTERS = 2
    private val PLACEHOLDERS = setOf("nota", "nota sin titulo", "sin titulo", "titulo", "recordatorio", "untitled")

    fun isMeaningful(title: String): Boolean =
        title.count { it.isLetter() } >= MIN_LETTERS && title.normalizeSpanish().trim('.', ' ') !in PLACEHOLDERS
}

/**
 * Short title: removes spoken fillers ("recuérdame que…") and the date phrase, keeps the
 * user's casing and accents, and never returns a title without words (":20" or "").
 */
internal object TitleBuilder {
    private const val TITLE_LENGTH = 60
    private const val ELLIPSIS = "…"

    /** Connectors left hanging before a removed date ("clase para [mañana] de Diseño"). */
    private val DANGLING = Regex("(?:\\s+(?:para|a|al|en|el|la|por))+\\s*$", RegexOption.IGNORE_CASE)
    private val CONTINUES_WITH_DE = Regex("^\\s*(?:de|del)\\b", RegexOption.IGNORE_CASE)

    fun build(rawText: String, dateFragments: List<String>): String {
        val text = rawText.normalizeSpaces()
        val withoutFillers = stripFillers(text)
        var title = withoutFillers
        dateFragments.forEach { fragment -> title = remove(title, fragment) }
        val cleaned = clean(title)
        val best = listOf(cleaned, clean(withoutFillers), text).firstOrNull(TitleRules::isMeaningful) ?: text
        return truncate(best.replaceFirstChar { it.uppercase() })
    }

    private fun remove(title: String, fragment: String): String {
        val index = title.foldAccentsKeepingLength().indexOf(fragment)
        if (index < 0) return title
        val before = title.substring(0, index)
        val after = title.substring(index + fragment.length)
        val joined = if (after.isBlank() || CONTINUES_WITH_DE.containsMatchIn(after)) before.replace(DANGLING, "") else before
        return "$joined $after"
    }

    private fun clean(title: String): String = title
        .normalizeSpaces()
        .replace(Regex("[\\s,.;:]+$"), "")
        .let { it.replace(TRAILING_CONNECTORS, "").ifEmpty { it } }
        .trim()

    private fun stripFillers(text: String): String {
        var start = 0
        SPOKEN_FILLERS.forEach { filler ->
            val remaining = text.substring(start)
            val match = filler.find(remaining.foldAccentsKeepingLength()) ?: return@forEach
            start += match.value.length
        }
        return text.substring(start.coerceAtMost(text.length))
    }

    private fun truncate(value: String): String {
        val limit = minOf(TITLE_LENGTH, Note.MAX_TITLE_LENGTH)
        return if (value.length <= limit) value else value.take(limit - 1).trimEnd() + ELLIPSIS
    }
}
