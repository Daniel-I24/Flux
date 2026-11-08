package com.flux.mobile.feature.settings

import com.flux.core.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ShortDurationTest {
    @Test
    fun `alarm lengths read like the snooze options`() {
        assertEquals(
            listOf(ShortDuration.Seconds(30), ShortDuration.Minutes(1), ShortDuration.Minutes(2), ShortDuration.Minutes(5)),
            AppSettings.ALARM_DURATION_OPTIONS.map(ShortDuration::of),
        )
    }

    @Test
    fun `a length that is not whole minutes stays in seconds`() {
        assertEquals(ShortDuration.Seconds(90), ShortDuration.of(90))
    }
}
