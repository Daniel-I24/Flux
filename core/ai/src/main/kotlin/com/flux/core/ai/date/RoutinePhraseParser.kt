package com.flux.core.ai.date

import com.flux.core.model.DailyRoutine
import com.flux.core.model.TimeOfDay
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * Times relative to the user's habits: "después de clase", "a la hora del almuerzo",
 * "antes de dormir", "al despertar". Uses the routine captured during onboarding.
 */
internal class RoutinePhraseParser(private val routine: DailyRoutine) {
    private data class Rule(val pattern: Regex, val resolve: (DayOfWeek) -> LocalTime?)

    private val rules = listOf(
        Rule(Regex("\\bdespues de(?:l)? (?:clase|clases|la universidad|el colegio|trabajo|la oficina)\\b")) { day ->
            routine.activityBlocks[day]?.end?.toLocalTime()
        },
        Rule(Regex("\\bantes de (?:clase|clases|ir a la universidad|entrar al trabajo)\\b")) { day ->
            routine.activityBlocks[day]?.start?.toLocalTime()?.minusMinutes(MARGIN_MINUTES)
        },
        Rule(Regex("\\b(?:a la hora del |en el |al )(?:almuerzo|almorzar)\\b")) { routine.lunch.toLocalTime() },
        Rule(Regex("\\b(?:a la hora del |en el |al )(?:desayuno|desayunar)\\b")) { routine.breakfast.toLocalTime() },
        Rule(Regex("\\b(?:a la hora de la |en la |al )(?:cena|cenar)\\b")) { routine.dinner.toLocalTime() },
        Rule(Regex("\\bantes de (?:dormir|acostarme)\\b")) { routine.sleep.toLocalTime().minusMinutes(MARGIN_MINUTES) },
        Rule(Regex("\\bal (?:despertar|levantarme)\\b")) { routine.wakeUp.toLocalTime() },
    )

    fun parse(normalized: String, day: DayOfWeek): TimeMatch? = rules.firstNotNullOfOrNull { rule ->
        rule.pattern.find(normalized)?.let { match -> rule.resolve(day)?.let { TimeMatch(it, match.value) } }
    }

    private fun TimeOfDay.toLocalTime(): LocalTime = LocalTime.of(hour, minute)

    private companion object {
        const val MARGIN_MINUTES = 30L
    }
}
