package com.flux.wear.sync

import com.flux.core.data.repository.OutboxRepository
import com.flux.core.data.repository.RoomCategoryRepository
import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.domain.usecase.AlarmTiming
import com.flux.core.model.ThemePalette
import com.flux.core.model.VibrationPattern
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.core.sync.protocol.WatchSnapshot
import com.flux.wear.data.WatchSettings
import com.flux.wear.data.WatchStateStore
import com.flux.wear.surface.SurfaceRefresher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

/** The theme picked last, on either device, wins. */
class SnapshotThemeTest {
    private val store = mockk<WatchStateStore>(relaxed = true) {
        coEvery { settings() } returns WatchSettings(palette = ThemePalette.ANDEAN_SUNSET, paletteChangedAt = 1_000)
    }
    private val applier = SnapshotApplier(
        mockk<RoomCategoryRepository>(relaxed = true),
        mockk<NoteRepository>(relaxed = true),
        mockk<AlarmScheduler>(relaxed = true),
        store,
        mockk<OutboxRepository>(relaxed = true) { coEvery { pending() } returns emptyList() },
        mockk<Clock>(relaxed = true),
        mockk<SurfaceRefresher>(relaxed = true),
        mockk<AlarmTiming>(relaxed = true),
    )

    private fun snapshot(generatedAt: Long, changedAt: Long) = WatchSnapshot(
        version = SyncProtocol.VERSION,
        generatedAt = generatedAt,
        accountState = AccountState.READY,
        linkedAccountId = null,
        userName = null,
        categories = emptyList(),
        agenda = emptyList(),
        snoozeMinutes = 5,
        vibration = VibrationPattern.entries.first(),
        quietHours = null,
        palette = ThemePalette.NIGHT_JUNGLE,
        paletteChangedAt = changedAt,
    )

    @Test
    fun `a theme picked later on the phone replaces the watch's`() = runTest {
        applier.apply(snapshot(generatedAt = 1, changedAt = 2_000))
        coVerify(exactly = 1) { store.updateSettings(any()) }
    }

    @Test
    fun `an older phone theme does not undo the one just picked on the watch`() = runTest {
        applier.apply(snapshot(generatedAt = 1, changedAt = 500))
        coVerify(exactly = 0) { store.updateSettings(any()) }
    }
}
