package com.flux.wear.feature.capture

import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LeadChoiceTest {
    @Test
    fun `tapping cycles from the phone's setting through every lead and back`() {
        var current: Int? = null
        val seen = mutableListOf(current)
        repeat(SEVEN) {
            current = nextLead(current)
            seen += current
        }
        assertEquals(listOf(null, 0, 5, 10, 15, 30, 60, null), seen)
    }

    @Test
    fun `choosing a lead stops the countdown and is kept for saving`() {
        val preview = CaptureState.Preview(
            text = "cita a las 3",
            classification = Classification("health", "Cita", confidence = 0.9, engine = ClassifierEngine.LOCAL),
            secondsLeft = 3,
            corrected = false,
        )
        val changed = preview.on(CaptureEvent.ChangeLead(15)) as CaptureState.Preview
        assertEquals(15, changed.alarmLeadMinutes)
        assertTrue(changed.paused)
    }

    private companion object {
        const val SEVEN = 7
    }
}
