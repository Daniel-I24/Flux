package com.flux.wear.sync

import com.flux.core.data.repository.OutboxRepository
import com.flux.core.data.repository.RoomCategoryRepository
import com.flux.core.domain.port.AlarmScheduler
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.model.VibrationPattern
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.core.sync.protocol.WatchSnapshot
import com.flux.wear.data.WatchStateStore
import com.flux.wear.surface.SurfaceRefresher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SnapshotOrderTest {
    private val categories = mockk<RoomCategoryRepository>(relaxed = true)
    private val notes = mockk<NoteRepository>(relaxed = true)
    private val outbox = mockk<OutboxRepository>(relaxed = true) { coEvery { pending() } returns emptyList() }
    private val applier = SnapshotApplier(
        categories,
        notes,
        mockk<AlarmScheduler>(relaxed = true),
        mockk<WatchStateStore>(relaxed = true),
        outbox,
        mockk<Clock>(relaxed = true),
        mockk<SurfaceRefresher>(relaxed = true),
    )

    private fun snapshot(generatedAt: Long) = WatchSnapshot(
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
    )

    @Test
    fun `an older snapshot arriving late does not undo a newer one`() = runTest {
        applier.apply(snapshot(generatedAt = 200))
        applier.apply(snapshot(generatedAt = 100))
        coVerify(exactly = 1) { notes.replaceAll(any()) }
    }

    @Test
    fun `a newer snapshot is applied`() = runTest {
        applier.apply(snapshot(generatedAt = 100))
        applier.apply(snapshot(generatedAt = 200))
        coVerify(exactly = 2) { notes.replaceAll(any()) }
    }
}
