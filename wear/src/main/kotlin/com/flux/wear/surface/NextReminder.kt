package com.flux.wear.surface

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.flux.core.domain.port.Clock
import com.flux.core.domain.usecase.ObserveUpcomingRemindersUseCase
import com.flux.wear.MainActivity
import com.flux.wear.R
import com.flux.wear.ui.DueDateText
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** What the tile and the complication show: the next pending reminder, already formatted. */
data class NextReminder(val title: String, val whenLabel: String, val shortTime: String)

class NextReminderProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeUpcoming: ObserveUpcomingRemindersUseCase,
    private val clock: Clock,
) {
    suspend fun next(): NextReminder? {
        val note = observeUpcoming(clock.now(), limit = 1).first().firstOrNull() ?: return null
        val dueAt = note.dueAt ?: return null
        val zone = clock.zone()
        val label = DueDateText.format(
            dueAt = dueAt,
            today = Instant.ofEpochMilli(clock.now()).atZone(zone).toLocalDate(),
            zone = zone,
            todayLabel = context.getString(R.string.agenda_today),
            tomorrowLabel = context.getString(R.string.date_tomorrow),
        )
        return NextReminder(note.title, label, SHORT_TIME.format(Instant.ofEpochMilli(dueAt).atZone(zone)))
    }

    private companion object {
        val SHORT_TIME: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }
}

/** Opens dictation directly (used by the complication; the tile uses a launch action). */
fun dictateIntent(context: Context): PendingIntent = PendingIntent.getActivity(
    context,
    0,
    Intent(context, MainActivity::class.java).setAction(MainActivity.ACTION_DICTATE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
)

/** Called whenever the agenda changes so the tile and complication never show stale data. */
class SurfaceRefresher @Inject constructor(@ApplicationContext private val context: Context) {
    fun refresh() {
        TileService.getUpdater(context).requestUpdate(FluxTileService::class.java)
        ComplicationDataSourceUpdateRequester
            .create(context, ComponentName(context, NextReminderComplicationService::class.java))
            .requestUpdateAll()
    }
}
