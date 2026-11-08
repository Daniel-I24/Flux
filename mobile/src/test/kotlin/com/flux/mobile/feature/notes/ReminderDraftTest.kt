package com.flux.mobile.feature.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** "Editar nota": the alarm switch, the date and time pickers and "Quitar". */
class ReminderDraftTest {
    private val noDate = ReminderDraft(dueAt = null, alarm = false)

    @Test
    fun `turning the alarm on without a date opens the picker and the pick keeps it on`() {
        val asking = noDate.setAlarm(true)
        assertTrue(asking.picking)
        assertTrue(asking.switchOn)
        val picked = asking.picked(AT)
        assertEquals(AT, picked.dueAt)
        assertTrue(picked.requiresAlarm)
        assertFalse(picked.picking)
    }

    @Test
    fun `cancelling the picker turns the switch back off`() {
        val cancelled = noDate.setAlarm(true).cancelPicker()
        assertFalse(cancelled.switchOn)
        assertFalse(cancelled.requiresAlarm)
        assertNull(cancelled.dueAt)
    }

    @Test
    fun `tapping Sin fecha and picking adds the date with its alarm`() {
        val picked = noDate.openPicker().picked(AT)
        assertEquals(AT, picked.dueAt)
        assertTrue(picked.requiresAlarm)
    }

    @Test
    fun `changing the date keeps the alarm as it was`() {
        val silent = ReminderDraft(dueAt = AT, alarm = false)
        assertFalse(silent.openPicker().picked(AT + 1).requiresAlarm)
        val ringing = ReminderDraft(dueAt = AT, alarm = true)
        assertTrue(ringing.openPicker().cancelPicker().requiresAlarm)
    }

    @Test
    fun `removing the date removes the alarm`() {
        val cleared = ReminderDraft(dueAt = AT, alarm = true).clearDate()
        assertNull(cleared.dueAt)
        assertFalse(cleared.requiresAlarm)
        assertFalse(cleared.switchOn)
    }

    private companion object {
        const val AT = 1_762_380_000_000L
    }
}
