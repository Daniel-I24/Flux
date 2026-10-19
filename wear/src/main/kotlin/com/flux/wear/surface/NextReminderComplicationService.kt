package com.flux.wear.surface

import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.MonochromaticImage
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.flux.core.designsystem.R as DesignR
import com.flux.wear.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Next reminder on the watch face; tapping it opens dictation. */
@AndroidEntryPoint
class NextReminderComplicationService : SuspendingComplicationDataSourceService() {
    @Inject lateinit var nextReminder: NextReminderProvider

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? =
        build(request.complicationType, nextReminder.next())

    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        build(type, NextReminder(getString(R.string.complication_preview_title), getString(R.string.agenda_today), PREVIEW_TIME))

    private fun build(type: ComplicationType, next: NextReminder?): ComplicationData? {
        val description = plain(
            next?.let { getString(R.string.complication_description, it.title, it.whenLabel) } ?: getString(R.string.home_no_reminders)
        )
        val icon = MonochromaticImage.Builder(Icon.createWithResource(this, DesignR.drawable.ic_stat_flux)).build()
        return when (type) {
            ComplicationType.SHORT_TEXT -> ShortTextComplicationData.Builder(
                plain(next?.shortTime ?: getString(R.string.app_name)),
                description
            )
                .setMonochromaticImage(icon)
                .setTapAction(dictateIntent(this))
                .build()
            ComplicationType.LONG_TEXT -> LongTextComplicationData.Builder(
                plain(next?.title ?: getString(R.string.complication_none)),
                description
            )
                .setTitle(plain(next?.whenLabel ?: getString(R.string.home_dictate)))
                .setMonochromaticImage(icon)
                .setTapAction(dictateIntent(this))
                .build()
            else -> null
        }
    }

    private fun plain(text: String) = PlainComplicationText.Builder(text).build()

    private companion object {
        const val PREVIEW_TIME = "08:00"
    }
}
