package com.flux.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/** Preferences shared by both apps (each keeps its own copy in DataStore). */
@Serializable
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val snoozeMinutes: Int = DEFAULT_SNOOZE_MINUTES,
    val vibration: VibrationPattern = VibrationPattern.DOUBLE,
    val defaultSound: SoundRef = SoundRef.Bundled(BundledSound.CHIME),
    val quietHoursEnabled: Boolean = true,
    val languageTag: String? = null,
) {
    companion object {
        const val DEFAULT_SNOOZE_MINUTES = 5
        val SNOOZE_OPTIONS = listOf(5, 10, 15, 30)
    }
}
