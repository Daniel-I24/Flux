package com.flux.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.flux.core.data.prefs.DataCipher
import com.flux.core.data.prefs.PreferencesStore
import com.flux.core.data.repository.DataStoreInstallationIdProvider
import com.flux.core.data.repository.DataStoreProfileRepository
import com.flux.core.data.repository.DataStoreSettingsRepository
import com.flux.core.model.AppSettings
import com.flux.core.model.Occupation
import com.flux.core.model.ThemeMode
import com.flux.core.model.UserProfile
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class PreferencesStoreTest {
    /** Reversible fake: lets us assert the profile is never stored in clear text. */
    private class ReversingCipher : DataCipher {
        override fun encrypt(plain: String) = plain.reversed()
        override fun decrypt(encoded: String) = encoded.reversed()
    }

    private fun TestScope.store(file: File = Files.createTempDirectory("prefs").resolve("test.preferences_pb").toFile()) =
        PreferencesStore(
            PreferenceDataStoreFactory.create(scope = backgroundScope) { file },
            ReversingCipher(),
        )

    @Test
    fun `settings default, update and persist`() = runTest {
        val settings = DataStoreSettingsRepository(store())
        assertEquals(AppSettings(), settings.getSettings())
        settings.update { it.copy(themeMode = ThemeMode.DARK, snoozeMinutes = 15) }
        assertEquals(ThemeMode.DARK, settings.getSettings().themeMode)
        assertEquals(15, settings.getSettings().snoozeMinutes)
    }

    @Test
    fun `profile is stored encrypted and read back`() = runTest {
        val file = Files.createTempDirectory("prefs").resolve("profile.preferences_pb").toFile()
        val profiles = DataStoreProfileRepository(store(file))
        assertNull(profiles.getProfile())
        profiles.saveProfile(UserProfile(displayName = "Daniela", age = 21, occupation = Occupation.STUDENT))
        assertEquals("Daniela", profiles.getProfile()?.displayName)
        assertFalse(file.readText(Charsets.ISO_8859_1).contains("Daniela"))
    }

    @Test
    fun `installation id is generated once`() = runTest {
        val provider = DataStoreInstallationIdProvider(store())
        assertEquals(provider.installationId(), provider.installationId())
    }
}
