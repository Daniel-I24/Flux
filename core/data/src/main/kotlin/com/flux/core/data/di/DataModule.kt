package com.flux.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.flux.core.data.local.FluxDatabase
import com.flux.core.data.prefs.DataCipher
import com.flux.core.data.prefs.KeystoreDataCipher
import com.flux.core.data.repository.DataStoreInstallationIdProvider
import com.flux.core.data.repository.DataStoreOnboardingRepository
import com.flux.core.data.repository.DataStoreProfileRepository
import com.flux.core.data.repository.DataStoreSettingsRepository
import com.flux.core.data.repository.RoomCategoryRepository
import com.flux.core.data.repository.RoomNoteRepository
import com.flux.core.data.system.SystemClock
import com.flux.core.data.system.UuidIdGenerator
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.NoteRepository
import com.flux.core.domain.repository.OnboardingRepository
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): FluxDatabase =
        Room.databaseBuilder(context, FluxDatabase::class.java, FluxDatabase.NAME).build()

    @Provides
    @Singleton
    fun dataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create { context.preferencesDataStoreFile(PREFERENCES_NAME) }

    private const val PREFERENCES_NAME = "flux_prefs"
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds abstract fun noteRepository(impl: RoomNoteRepository): NoteRepository

    @Binds abstract fun categoryRepository(impl: RoomCategoryRepository): CategoryRepository

    @Binds abstract fun settingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    @Binds abstract fun profileRepository(impl: DataStoreProfileRepository): ProfileRepository

    @Binds abstract fun onboardingRepository(impl: DataStoreOnboardingRepository): OnboardingRepository

    @Binds abstract fun installationId(impl: DataStoreInstallationIdProvider): InstallationIdProvider

    @Binds abstract fun clock(impl: SystemClock): Clock

    @Binds abstract fun ids(impl: UuidIdGenerator): IdGenerator

    @Binds abstract fun cipher(impl: KeystoreDataCipher): DataCipher
}
