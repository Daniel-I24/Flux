package com.flux.core.ai.remote

import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.model.TimeOfDay
import java.time.Instant
import java.time.format.DateTimeFormatter

private fun TimeOfDay.format(): String = "%02d:%02d".format(hour, minute)

/** Only what the AI needs: occupation and habitual times (no name, no age). */
internal fun ClassificationRequest.toRemote(): RemoteClassifyRequest {
    val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
    return RemoteClassifyRequest(
        text = text,
        now = now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        timeZone = zone.id,
        categories = categories.map { RemoteCategory(id = it.id, name = it.customName) },
        profile = profile?.let { user ->
            RemoteProfile(
                occupation = user.occupation.name,
                wakeUp = user.routine.wakeUp.format(),
                sleep = user.routine.sleep.format(),
                breakfast = user.routine.breakfast.format(),
                lunch = user.routine.lunch.format(),
                dinner = user.routine.dinner.format(),
                activityEndToday = user.routine.activityBlocks[now.dayOfWeek]?.end?.format(),
            )
        },
    )
}
