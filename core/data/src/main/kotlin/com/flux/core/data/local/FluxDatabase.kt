package com.flux.core.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.flux.core.data.local.dao.CategoryDao
import com.flux.core.data.local.dao.NoteDao
import com.flux.core.data.local.dao.OutboxDao
import com.flux.core.data.local.entity.CategoryEntity
import com.flux.core.data.local.entity.NoteEntity
import com.flux.core.data.local.entity.OutboxEntity

/** Same schema on phone and watch: the phone owns notes, the watch caches its agenda and queues captures. */
@Database(
    entities = [NoteEntity::class, CategoryEntity::class, OutboxEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun notes(): NoteDao
    abstract fun categories(): CategoryDao
    abstract fun outbox(): OutboxDao

    companion object {
        const val NAME = "flux.db"
    }
}
