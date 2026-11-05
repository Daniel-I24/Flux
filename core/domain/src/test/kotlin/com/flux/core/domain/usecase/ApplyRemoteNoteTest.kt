package com.flux.core.domain.usecase

import com.flux.core.domain.FakeAlarms
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.FixedClock
import com.flux.core.domain.note
import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyRemoteNoteTest {
    private val notes = FakeNoteRepository()
    private val apply = ApplyRemoteNoteUseCase(notes, FakeAlarms(), FixedClock(nowMillis = 1_000))
    private val tone = SoundRef.Custom("tone_1.mp3", "Mi tono")

    @Test
    fun `a revision from the watch keeps the custom tone chosen on the phone`() = runTest {
        notes.upsert(note("a", updatedAt = 1).copy(sound = tone))
        apply(note("a", updatedAt = 2).copy(completed = true))
        assertEquals(tone, notes.getNote("a")?.sound)
        assertEquals(true, notes.getNote("a")?.completed)
    }

    @Test
    fun `a sound that comes in the revision is respected`() = runTest {
        notes.upsert(note("a", updatedAt = 1).copy(sound = tone))
        val bell = SoundRef.Bundled(BundledSound.BELL)
        apply(note("a", updatedAt = 2).copy(sound = bell))
        assertEquals(bell, notes.getNote("a")?.sound)
    }
}
