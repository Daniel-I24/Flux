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
    /** How long a reminder rings (and the watch vibrates) before it stops by itself. */
    val alarmDurationSeconds: Int = DEFAULT_ALARM_SECONDS,
    /** The welcome tutorial was finished or skipped (it can be repeated from Ajustes). */
    val tutorialDone: Boolean = false,
) {
    companion object {
        const val DEFAULT_SNOOZE_MINUTES = 5
        val SNOOZE_OPTIONS = listOf(5, 10, 15, 30)
        const val DEFAULT_ALARM_SECONDS = 60
        val ALARM_DURATION_OPTIONS = listOf(30, 60, 120, 300)
    }
}
