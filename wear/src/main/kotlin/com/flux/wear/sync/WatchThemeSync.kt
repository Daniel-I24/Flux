package com.flux.wear.sync

import com.flux.core.domain.port.Clock
import com.flux.core.model.ThemePalette
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.wear.data.WatchStateStore
import javax.inject.Inject

/**
 * A theme picked on the watch: applied here at once and sent to the phone, which keeps the
 * newest choice and returns it in the next snapshot. Offline, the next pick or snapshot settles it.
 */
class WatchThemeSync @Inject constructor(
    private val store: WatchStateStore,
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
    private val clock: Clock,
) {
    suspend fun pick(palette: ThemePalette) {
        val changedAt = clock.now()
        store.updateSettings { it.copy(palette = palette, paletteChangedAt = changedAt) }
        val message = factory.themeChanged(palette, changedAt)
        gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = true).forEach { gateway.send(it.id, message) }
    }
}
