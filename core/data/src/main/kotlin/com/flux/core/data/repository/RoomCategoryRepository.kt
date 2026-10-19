package com.flux.core.data.repository

import com.flux.core.data.local.FluxDatabase
import com.flux.core.data.local.toEntity
import com.flux.core.data.local.toModel
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.model.Category
import com.flux.core.model.DefaultCategory
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomCategoryRepository @Inject constructor(database: FluxDatabase) : CategoryRepository {
    private val dao = database.categories()

    override fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }.distinctUntilChanged()

    override suspend fun getCategories(): List<Category> = dao.getAll().map { it.toModel() }

    override suspend fun upsert(category: Category) = dao.upsert(category.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun ensureDefaults() {
        val existing = dao.getAll().map { it.id }.toSet()
        val missing = DefaultCategory.all().filterNot { it.id in existing }
        if (missing.isNotEmpty()) dao.upsertAll(missing.map { it.toEntity() })
    }

    /** Watch side: replace its cache with the categories published by the phone. */
    suspend fun replaceAll(categories: List<Category>) {
        val incoming = categories.map { it.id }.toSet()
        dao.getAll().filterNot { it.id in incoming }.forEach { dao.delete(it.id) }
        dao.upsertAll(categories.map { it.toEntity() })
    }
}
