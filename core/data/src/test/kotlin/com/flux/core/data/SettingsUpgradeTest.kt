package com.flux.core.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.flux.core.data.prefs.DataCipher
import com.flux.core.data.prefs.PreferencesStore
import com.flux.core.data.repository.DataStoreSettingsRepository
import com.flux.core.model.AppSettings
import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef
import com.flux.core.model.ThemeMode
import com.flux.core.model.VibrationPattern
import java.nio.file.Files
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/** Exactly what v1.0.0 wrote to DataStore: updating must keep it and default the new fields. */
class SettingsUpgradeTest {
    private object NoCipher : DataCipher {
        override fun encrypt(plain: String) = plain
        override fun decrypt(encoded: String) = encoded
    }

    @Test
    fun `settings saved by v1_0_0 survive and new fields take their defaults`() = runTest {
        val file = Files.createTempDirectory("upgrade").resolve("settings.preferences_pb").toFile()
        val dataStore = PreferenceDataStoreFactory.create(scope = backgroundScope) { file }
        dataStore.edit { it[stringPreferencesKey("settings")] = V1_SETTINGS }

        val settings = DataStoreSettingsRepository(PreferencesStore(dataStore, NoCipher)).getSettings()

        assertEquals(ThemeMode.DARK, settings.themeMode)
        assertEquals(15, settings.snoozeMinutes)
        assertEquals(VibrationPattern.LONG, settings.vibration)
        assertEquals(SoundRef.Bundled(BundledSound.BELL), settings.defaultSound)
        assertFalse(settings.quietHoursEnabled)
        assertEquals(AppSettings.DEFAULT_ALARM_SECONDS, settings.alarmDurationSeconds)
        assertFalse(settings.tutorialDone)
    }

    private companion object {
        const val V1_SETTINGS = """{"themeMode":"DARK","snoozeMinutes":15,"vibration":"LONG",""" +
            """"defaultSound":{"type":"com.flux.core.model.SoundRef.Bundled","sound":"BELL"},"quietHoursEnabled":false}"""
    }
}
