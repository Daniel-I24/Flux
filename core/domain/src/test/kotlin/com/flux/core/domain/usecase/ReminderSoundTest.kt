package com.flux.core.domain.usecase

import com.flux.core.domain.FakeCategoryRepository
import com.flux.core.domain.FakeNoteRepository
import com.flux.core.domain.note
import com.flux.core.model.BundledSound
import com.flux.core.model.DefaultCategory
import com.flux.core.model.SoundRef
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderSoundTest {
    private val default = SoundRef.Bundled(BundledSound.CHIME)
    private val bell = SoundRef.Bundled(BundledSound.BELL)
    private val soft = SoundRef.Bundled(BundledSound.SOFT)
    private val notes = FakeNoteRepository()
    private val categories = FakeCategoryRepository(
        DefaultCategory.all().map { if (it.id == "health") it.copy(sound = bell) else it },
    )
    private val getReminder = GetReminderUseCase(notes, categories)

    @Test
    fun `the note's own sound wins over the category and the default`() = runTest {
        notes.upsert(note("n1", categoryId = "health").copy(sound = soft))
        assertEquals(soft, getReminder("n1", default)?.sound)
    }

    @Test
    fun `without its own sound a note uses its category's sound`() = runTest {
        notes.upsert(note("n1", categoryId = "health"))
        val reminder = getReminder("n1", default)
        assertEquals(bell, reminder?.sound)
        assertEquals("health", reminder?.category?.id)
    }

    @Test
    fun `otherwise the app default is used`() = runTest {
        notes.upsert(note("n1", categoryId = "tasks"))
        assertEquals(default, getReminder("n1", default)?.sound)
    }

    @Test
    fun `an unknown note has no reminder`() = runTest {
        assertNull(getReminder("missing", default))
    }
}
