package com.flux.core.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.flux.core.data.local.FluxDatabase
import com.flux.core.data.repository.RoomCategoryRepository
import com.flux.core.data.repository.RoomNoteRepository
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomRepositoriesTest {
    private val database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), FluxDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    private val notes = RoomNoteRepository(database)
    private val categories = RoomCategoryRepository(database)

    private fun note(id: String, updatedAt: Long, dueAt: Long? = null, alarm: Boolean = false) = Note(
        id = id,
        text = "Texto",
        title = "Nota $id",
        categoryId = "tasks",
        dueAt = dueAt,
        requiresAlarm = alarm,
        source = DeviceSource.WATCH,
        engine = ClassifierEngine.LOCAL,
        createdAt = 1L,
        updatedAt = updatedAt,
    )

    @After fun close() = database.close()

    @Test
    fun `upsertIfNewer keeps the latest revision`() = runTest {
        assertTrue(notes.upsertIfNewer(note("a", updatedAt = 10)))
        assertFalse(notes.upsertIfNewer(note("a", updatedAt = 10)))
        assertFalse(notes.upsertIfNewer(note("a", updatedAt = 5)))
        assertTrue(notes.upsertIfNewer(note("a", updatedAt = 20).copy(title = "Nueva")))
        assertEquals("Nueva", notes.getNote("a")?.title)
    }

    @Test
    fun `soft deleted notes disappear from the active list`() = runTest {
        notes.observeNotes().test {
            assertEquals(emptyList<Note>(), awaitItem())
            notes.upsert(note("a", updatedAt = 1))
            assertEquals(1, awaitItem().size)
            notes.upsert(note("a", updatedAt = 2).copy(deletedAt = 2))
            assertEquals(0, awaitItem().size)
        }
    }

    @Test
    fun `upcoming and pending alarms are filtered and ordered`() = runTest {
        notes.upsert(note("late", updatedAt = 1, dueAt = 300, alarm = true))
        notes.upsert(note("soon", updatedAt = 1, dueAt = 200, alarm = true))
        notes.upsert(note("past", updatedAt = 1, dueAt = 50))
        notes.upsert(note("done", updatedAt = 1, dueAt = 250, alarm = true).copy(completed = true))
        notes.observeUpcoming(fromMillis = 100, limit = 10).test {
            assertEquals(listOf("soon", "late"), awaitItem().map { it.id })
        }
        assertEquals(setOf("late", "soon"), notes.getPendingAlarms().map { it.id }.toSet())
    }

    @Test
    fun `default categories are inserted once and reassignment works`() = runTest {
        categories.ensureDefaults()
        categories.ensureDefaults()
        assertEquals(DefaultCategory.entries.size, categories.getCategories().size)
        notes.upsert(note("a", updatedAt = 1))
        notes.reassignCategory("tasks", "info", updatedAt = 9)
        assertEquals("info", notes.getNote("a")?.categoryId)
    }
}
