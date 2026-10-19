package com.flux.wear.surface

import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.material3.titleCard
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.LayoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.flux.wear.MainActivity
import com.flux.wear.R
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future

/** "Dictar nota" plus the next reminder, one swipe away from the watch face. */
@AndroidEntryPoint
class FluxTileService : TileService() {
    @Inject lateinit var nextReminder: NextReminderProvider

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> = scope.future {
        val layout = layout(requestParams.deviceConfiguration, nextReminder.next())
        TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setFreshnessIntervalMillis(FRESHNESS_MS)
            .setTileTimeline(TimelineBuilders.Timeline.fromLayoutElement(layout))
            .build()
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> =
        scope.future { ResourceBuilders.Resources.Builder().setVersion(RESOURCES_VERSION).build() }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun layout(device: DeviceParameters, next: NextReminder?): LayoutElementBuilders.LayoutElement = materialScope(this, device) {
        primaryLayout(
            titleSlot = { text(LayoutString(getString(R.string.app_name))) },
            mainSlot = { nextReminderCard(next) },
            bottomSlot = {
                textEdgeButton(onClick = clickable(launch(MainActivity.START_DICTATE), DICTATE_ID)) {
                    text(LayoutString(getString(R.string.home_dictate)))
                }
            },
        )
    }

    private fun MaterialScope.nextReminderCard(next: NextReminder?): LayoutElementBuilders.LayoutElement = titleCard(
        onClick = clickable(launch(start = null), OPEN_ID),
        title = { text(LayoutString((next?.title ?: getString(R.string.home_no_reminders)).take(MAX_TITLE_CHARS))) },
        content = { text(LayoutString(next?.whenLabel ?: getString(R.string.home_next_reminder))) },
    )

    private fun launch(start: String?): ActionBuilders.LaunchAction {
        val activity = ActionBuilders.AndroidActivity.Builder()
            .setPackageName(packageName)
            .setClassName(MainActivity::class.java.name)
        if (start != null) {
            activity.addKeyToExtraMapping(MainActivity.EXTRA_START, ActionBuilders.AndroidStringExtra.Builder().setValue(start).build())
        }
        return ActionBuilders.LaunchAction.Builder().setAndroidActivity(activity.build()).build()
    }

    private companion object {
        const val RESOURCES_VERSION = "1"
        const val FRESHNESS_MS = 15 * 60 * 1000L
        const val MAX_TITLE_CHARS = 40
        const val DICTATE_ID = "dictate"
        const val OPEN_ID = "open"
    }
}
