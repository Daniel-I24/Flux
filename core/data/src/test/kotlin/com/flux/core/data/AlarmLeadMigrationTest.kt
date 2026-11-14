package com.flux.core.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import com.flux.core.data.local.FluxDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** v1.1 (schema 2) → v1.2 (schema 3): notes gain their own alarm lead, empty so they follow the global one. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AlarmLeadMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FluxDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun `migrating from schema 2 keeps every note and leaves its lead empty`() {
        helper.createDatabase(DB, 2).apply {
            execSQL(
                "INSERT INTO notes (id, text, title, categoryId, dueAt, priority, requiresAlarm, completed, source, engine, " +
                    "categoryCorrected, soundJson, createdAt, updatedAt, deletedAt) VALUES " +
                    "('n1', 'cita a las 3', 'Cita', 'health', 1000, 'HIGH', 1, 0, 'WATCH', 'LOCAL', 0, NULL, 10, 20, NULL)",
            )
            close()
        }
        helper.runMigrationsAndValidate(DB, 3, true).apply {
            query("SELECT title, dueAt, priority, alarmLeadMinutes FROM notes WHERE id = 'n1'").use { row ->
                row.moveToFirst()
                assertEquals("Cita", row.getString(0))
                assertEquals(1000L, row.getLong(1))
                assertEquals("HIGH", row.getString(2))
                assertTrue(row.isNull(3))
            }
            execSQL("UPDATE notes SET alarmLeadMinutes = 15 WHERE id = 'n1'")
            query("SELECT alarmLeadMinutes FROM notes WHERE id = 'n1'").use { row ->
                row.moveToFirst()
                assertEquals(15, row.getInt(0))
            }
            close()
        }
    }

    private companion object {
        const val DB = "lead-migration-test"
    }
}
