package com.flux.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.flux.core.data.local.entity.CategoryEntity
import com.flux.core.data.local.entity.NoteEntity
import com.flux.core.data.local.entity.OutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun observe(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: String): NoteEntity?

    @Query(
        "SELECT * FROM notes WHERE deletedAt IS NULL AND completed = 0 AND dueAt IS NOT NULL AND dueAt >= :from " +
            "ORDER BY dueAt ASC LIMIT :limit",
    )
    fun observeUpcoming(from: Long, limit: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE deletedAt IS NULL AND completed = 0 AND requiresAlarm = 1 AND dueAt IS NOT NULL")
    suspend fun pendingAlarms(): List<NoteEntity>

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("UPDATE notes SET categoryId = :to, updatedAt = :updatedAt WHERE categoryId = :from")
    suspend fun reassign(from: String, to: String, updatedAt: Long)

    @Query("DELETE FROM notes")
    suspend fun clear()
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY position ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY position ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox ORDER BY createdAt ASC")
    suspend fun pending(): List<OutboxEntity>

    @Query("SELECT COUNT(*) FROM outbox")
    fun observeCount(): Flow<Int>

    @Upsert
    suspend fun upsert(item: OutboxEntity)

    @Query("DELETE FROM outbox WHERE noteId = :noteId")
    suspend fun remove(noteId: String)
}
