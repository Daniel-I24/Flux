package com.flux.core.ai.date

import com.flux.core.ai.text.normalizeSpanish
import com.flux.core.domain.port.ScheduleGap
import com.flux.core.domain.port.ScheduleGapDetector
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * Spanish reminders left incomplete. A day without a time ("el viernes", "mañana a las") asks for
 * the time on that day; a time cut off ("a las", "a las de la tarde"), a day cut off ("el") or a
 * "recuérdame" with no date asks for both. Anything the date parser reads as a time is explicit.
 */
class SpanishScheduleGapDetector @Inject constructor() : ScheduleGapDetector {
    override fun detect(text: String, now: ZonedDateTime): ScheduleGap? {
        val parsed = SpanishDateParser().parse(text, now)
        if (parsed != null) return if (parsed.hasExplicitTime) null else ScheduleGap(parsed.dateTime.toLocalDate())
        val normalized = text.normalizeSpanish().trimEnd { it in TRAILING }
        val incomplete = CUT_OFF.any { it.containsMatchIn(normalized) } || REMINDER_VERB.containsMatchIn(normalized)
        return if (incomplete) ScheduleGap(day = null) else null
    }

    private companion object {
        const val TRAILING = ".,;:!?… "
        private const val PART = """(?:de|en|por)\s+la\s+(?:manana|tarde|noche)"""
        val CUT_OFF = listOf(
            // "salir a las", "llamar a eso de las": the hour never came.
            Regex("""\b(?:a|para|a eso de|como a)\s+las?$"""),
            // "a las de la tarde": the part of the day without the hour.
            Regex("""\b(?:a|para|a eso de|como a)\s+las\s+$PART\b"""),
            // "llamar a Juan el", "para el": the day never came.
            Regex("""\b(?:el|para el|este|el proximo)$"""),
        )
        val REMINDER_VERB = Regex("""\b(?:recuerdame|recordarme|recuerdeme|avisame|no (?:me )?(?:dejes )?olvid(?:ar|es))\b""")
    }
}
