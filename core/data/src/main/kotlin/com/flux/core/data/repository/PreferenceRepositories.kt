package com.flux.core.data.repository

import com.flux.core.data.prefs.PreferencesStore
import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.repository.OnboardingRepository
import com.flux.core.domain.repository.OnboardingStep
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.domain.repository.SettingsRepository
import com.flux.core.model.AppSettings
import com.flux.core.model.UserProfile
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.builtins.serializer

private object Keys {
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val ONBOARDING = "onboarding_step"
    const val INSTALLATION_ID = "installation_id"
}

class DataStoreSettingsRepository @Inject constructor(private val store: PreferencesStore) : SettingsRepository {
    private val mutex = Mutex()

    override fun observeSettings(): Flow<AppSettings> =
        store.observe(Keys.SETTINGS, AppSettings.serializer()).map { it ?: AppSettings() }

    override suspend fun getSettings(): AppSettings = store.get(Keys.SETTINGS, AppSettings.serializer()) ?: AppSettings()

    override suspend fun update(transform: (AppSettings) -> AppSettings) = mutex.withLock {
        store.put(Keys.SETTINGS, AppSettings.serializer(), transform(getSettings()))
    }
}

/** Profile contains personal data (name, age): stored encrypted. */
class DataStoreProfileRepository @Inject constructor(private val store: PreferencesStore) : ProfileRepository {
    override fun observeProfile(): Flow<UserProfile?> = store.observe(Keys.PROFILE, UserProfile.serializer(), sensitive = true)

    override suspend fun getProfile(): UserProfile? = store.get(Keys.PROFILE, UserProfile.serializer(), sensitive = true)

    override suspend fun saveProfile(profile: UserProfile) =
        store.put(Keys.PROFILE, UserProfile.serializer(), profile, sensitive = true)

    override suspend fun clear() = store.remove(Keys.PROFILE)
}

class DataStoreOnboardingRepository @Inject constructor(private val store: PreferencesStore) : OnboardingRepository {
    override fun observeStep(): Flow<OnboardingStep> = store.observe(Keys.ONBOARDING, String.serializer())
        .map { name -> OnboardingStep.entries.firstOrNull { it.name == name } ?: OnboardingStep.WELCOME }

    override suspend fun setStep(step: OnboardingStep) = store.put(Keys.ONBOARDING, String.serializer(), step.name)
}

/** Random id generated once per installation (never derived from hardware identifiers). */
class DataStoreInstallationIdProvider @Inject constructor(private val store: PreferencesStore) : InstallationIdProvider {
    private val mutex = Mutex()

    override suspend fun installationId(): String = mutex.withLock {
        store.get(Keys.INSTALLATION_ID, String.serializer()) ?: UUID.randomUUID().toString().also {
            store.put(Keys.INSTALLATION_ID, String.serializer(), it)
        }
    }
}
