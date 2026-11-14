package com.flux.wear.alarm

import com.flux.core.data.alarm.AlarmLog
import com.flux.core.domain.usecase.GetNoteUseCase
import com.flux.wear.surface.SurfaceRefresher
import javax.inject.Inject

/**
 * One entry point for "this reminder is due on the watch", reached from the watch's own exact
 * alarm and from the phone's high-priority "ringing" message. Whichever arrives first alerts;
 * [WatchAlertNotifier] ignores the second one.
 */
class WatchReminderAlert @Inject constructor(
    private val getNote: GetNoteUseCase,
    private val notifier: WatchAlertNotifier,
    private val surfaces: SurfaceRefresher,
) {
    suspend fun fire(noteId: String, source: String) {
        val note = getNote(noteId)
        if (note?.hasPendingAlarm == true) {
            AlarmLog.info("watch alarm due source=$source")
            notifier.show(note)
        } else {
            AlarmLog.info("watch alarm ignored source=$source reason=handled")
        }
        // The reminder is now overdue: the tile and the complication move to the next one.
        surfaces.refresh()
    }
}
