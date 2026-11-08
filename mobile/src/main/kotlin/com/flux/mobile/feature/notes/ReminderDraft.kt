package com.flux.mobile.feature.notes

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

/**
 * Date and alarm of the note being edited. Turning the alarm on without a date opens the picker
 * first, and cancelling it leaves the alarm off; adding a date to a note that had none turns the
 * alarm on (the user can still turn it off). Removing the date removes the alarm.
 */
data class ReminderDraft(val dueAt: Long?, val alarm: Boolean, val picking: Boolean = false, val alarmOnPick: Boolean = false) {
    /** What the switch shows: on while the picker opened by it is still open. */
    val switchOn: Boolean get() = (alarm && dueAt != null) || (picking && alarmOnPick)

    val requiresAlarm: Boolean get() = alarm && dueAt != null

    fun openPicker() = copy(picking = true, alarmOnPick = dueAt == null || alarm)

    fun setAlarm(on: Boolean) = when {
        !on -> copy(alarm = false)
        dueAt == null -> copy(picking = true, alarmOnPick = true)
        else -> copy(alarm = true)
    }

    fun picked(at: Long) = copy(dueAt = at, alarm = alarmOnPick, picking = false, alarmOnPick = false)

    fun cancelPicker() = copy(picking = false, alarmOnPick = false, alarm = alarm && dueAt != null)

    fun clearDate() = copy(dueAt = null, alarm = false, picking = false, alarmOnPick = false)

    companion object {
        val saver: Saver<ReminderDraft, Any> = listSaver(
            save = { listOf(it.dueAt, it.alarm, it.picking, it.alarmOnPick) },
            restore = { ReminderDraft(it[0] as Long?, it[1] as Boolean, it[2] as Boolean, it[3] as Boolean) },
        )
    }
}
