package com.flux.core.domain.usecase

import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.ScheduleGapDetector
import com.flux.core.model.Ambiguity
import com.flux.core.model.Classification
import com.flux.core.model.DailyRoutine
import java.time.Instant
import javax.inject.Inject

/**
 * Shared by every classifier and both devices: a reminder with its time left incomplete is never
 * saved silently. The date the classifier guessed is dropped and "¿Cuándo te aviso?" is asked first,
 * with suggestions from the profile. An explicit time in the text always wins (no question).
 */
class AskReminderTimeUseCase @Inject constructor(private val detector: ScheduleGapDetector, private val clock: Clock) {
    operator fun invoke(text: String, classification: Classification, routine: DailyRoutine?): Classification {
        if (classification.ambiguities.any { it is Ambiguity.ReminderTime }) return classification
        val now = Instant.ofEpochMilli(clock.now()).atZone(clock.zone())
        val gap = detector.detect(text, now) ?: return classification
        val options = ReminderSuggestions.of(gap.day, now, routine ?: DailyRoutine())
        val others = classification.ambiguities.filterNot { it is Ambiguity.DueTime }
        return classification.copy(dueAt = null, requiresAlarm = false, ambiguities = listOf(Ambiguity.ReminderTime(options)) + others)
    }
}
