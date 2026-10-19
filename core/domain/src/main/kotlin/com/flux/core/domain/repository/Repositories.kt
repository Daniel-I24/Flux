package com.flux.core.domain.repository

import com.flux.core.model.AppSettings
import com.flux.core.model.Category
import com.flux.core.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun getCategories(): List<Category>
    suspend fun upsert(category: Category)
    suspend fun delete(id: String)

    /** Inserts the default categories the first time (idempotent). */
    suspend fun ensureDefaults()
}

interface ProfileRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun getProfile(): UserProfile?
    suspend fun saveProfile(profile: UserProfile)
    suspend fun clear()
}

interface SettingsRepository {
    fun observeSettings(): Flow<AppSettings>
    suspend fun getSettings(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)
}

enum class OnboardingStep { WELCOME, ACCOUNT, PROFILE, ROUTINE, PERMISSIONS, WATCH, DONE }

interface OnboardingRepository {
    fun observeStep(): Flow<OnboardingStep>
    suspend fun setStep(step: OnboardingStep)
}
