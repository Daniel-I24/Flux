package com.flux.core.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.flux.core.data.local.FluxDatabase
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** v1.0.0 shipped schema 2 and earlier builds schema 1: both must open keeping every note. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FluxDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun `migrating from schema 1 keeps notes, categories and the offline queue`() {
        helper.createDatabase(DB, 1).apply {
            execSQL(
                "INSERT INTO notes (id, text, title, categoryId, dueAt, priority, requiresAlarm, completed, source, engine, " +
                    "categoryCorrected, createdAt, updatedAt, deletedAt) VALUES " +
                    "('n1', 'cita a las 3', 'Cita', 'health', 1000, 'NORMAL', 1, 0, 'WATCH', 'LOCAL', 0, 10, 20, NULL)",
            )
            execSQL(
                "INSERT INTO categories (id, defaultKey, customName, colorArgb, icon, soundJson, position) " +
                    "VALUES ('gym', NULL, 'Gimnasio', 42, 'FITNESS', NULL, 7)",
            )
            execSQL("INSERT INTO outbox (noteId, payload, createdAt, attempts) VALUES ('n1', x'01', 5, 2)")
            close()
        }
        helper.runMigrationsAndValidate(DB, 2, true).apply {
            query("SELECT title, dueAt, soundJson FROM notes WHERE id = 'n1'").use { row ->
                row.moveToFirst()
                assertEquals("Cita", row.getString(0))
                assertEquals(1000L, row.getLong(1))
                assertEquals(true, row.isNull(2))
            }
            query("SELECT customName FROM categories WHERE id = 'gym'").use { row ->
                row.moveToFirst()
                assertEquals("Gimnasio", row.getString(0))
            }
            query("SELECT attempts FROM outbox WHERE noteId = 'n1'").use { row ->
                row.moveToFirst()
                assertEquals(2, row.getInt(0))
            }
            close()
        }
    }

    @Test
    fun `the app opens a database written by v1_0_0 and reads its notes`() = runTest {
        helper.createDatabase(DB, 2).apply {
            execSQL(
                "INSERT INTO notes (id, text, title, categoryId, dueAt, priority, requiresAlarm, completed, source, engine, " +
                    "categoryCorrected, soundJson, createdAt, updatedAt, deletedAt) VALUES " +
                    "('n2', 'comprar pan', 'Comprar pan', 'shopping', NULL, 'NORMAL', 0, 1, 'PHONE', 'REMOTE', 1, NULL, 10, 20, NULL)",
            )
            close()
        }
        val database = Room.databaseBuilder(ApplicationProvider.getApplicationContext(), FluxDatabase::class.java, DB)
            .allowMainThreadQueries()
            .build()
        val note = database.notes().get("n2")
        assertEquals("Comprar pan", note?.title)
        assertNull(note?.dueAt)
        database.close()
    }

    private companion object {
        const val DB = "migration-test"
    }
}
