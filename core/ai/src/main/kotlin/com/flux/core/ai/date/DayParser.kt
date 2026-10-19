package com.flux.core.ai.date

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month

/**
 * Detects the day: "hoy", "mañana", "pasado mañana", weekdays, "el 25 de octubre",
 * and parts of the day ("esta tarde", "mañana en la noche"). Expects normalized text.
 * "de la mañana" (a.m.) is never read as tomorrow.
 */
internal class DayParser(private val partsOfDay: Map<String, LocalTime>) {
    private val weekdays = mapOf(
        "lunes" to DayOfWeek.MONDAY,
        "martes" to DayOfWeek.TUESDAY,
        "miercoles" to DayOfWeek.WEDNESDAY,
        "jueves" to DayOfWeek.THURSDAY,
        "viernes" to DayOfWeek.FRIDAY,
        "sabado" to DayOfWeek.SATURDAY,
        "domingo" to DayOfWeek.SUNDAY,
    )
    private val months = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
    )
    private val amPhrase = Regex("\\b(de|por) la manana\\b")
    private val dayAfterTomorrow = Regex("\\bpasado manana(?:\\s+(?:en|por) la\\s+(tarde|noche|manana))?\\b")
    private val tomorrow = Regex("\\bmanana(?:\\s+(?:en|por) la\\s+(tarde|noche|manana))?\\b")
    private val today = Regex("\\b(?:esta|en la|por la)\\s+(tarde|noche|manana)\\b|\\bhoy\\b")
    private val weekday = Regex(
        "\\b(?:el\\s+|este\\s+|el proximo\\s+|el siguiente\\s+)?(${weekdays.keys.joinToString("|")})(\\s+(?:proximo|que viene))?\\b",
    )
    private val dayOfMonth = Regex("\\bel\\s+(\\d{1,2})\\s+de\\s+(${months.joinToString("|")})\\b")

    fun parse(normalized: String, today: LocalDate): DayMatch? {
        dayAfterTomorrow.find(normalized)?.let { return DayMatch(today.plusDays(2), part(it.groupValues[1]), it.value) }
        val withoutAm = normalized.replace(amPhrase) { " ".repeat(it.value.length) }
        tomorrow.find(withoutAm)?.let { return DayMatch(today.plusDays(1), part(it.groupValues[1]), it.value) }
        this.today.find(withoutAm)?.let { return DayMatch(today, part(it.groupValues[1]), it.value) }
        dayOfMonth.find(normalized)?.let { return parseDayOfMonth(it, today) }
        return weekday.find(normalized)?.let { parseWeekday(it, today) }
    }

    private fun part(name: String?): LocalTime? = name?.takeIf { it.isNotEmpty() }?.let(partsOfDay::get)

    private fun parseWeekday(match: MatchResult, today: LocalDate): DayMatch? {
        val target = weekdays[match.groupValues[1]] ?: return null
        val explicitNext = match.value.contains("proximo") || match.value.contains("siguiente") || match.value.contains("que viene")
        var delta = (target.value - today.dayOfWeek.value + DAYS_PER_WEEK) % DAYS_PER_WEEK
        if (delta == 0 && explicitNext) delta = DAYS_PER_WEEK
        return DayMatch(today.plusDays(delta.toLong()), null, match.value.trim())
    }

    private fun parseDayOfMonth(match: MatchResult, today: LocalDate): DayMatch? {
        val day = match.groupValues[1].toInt()
        val month = Month.of(months.indexOf(match.groupValues[2]) + 1)
        val candidate = runCatching { LocalDate.of(today.year, month, day) }.getOrNull() ?: return null
        val date = if (candidate.isBefore(today)) candidate.plusYears(1) else candidate
        return DayMatch(date, null, match.value)
    }

    private companion object {
        const val DAYS_PER_WEEK = 7
    }
}
