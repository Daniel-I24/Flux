package com.flux.core.sync.protocol

import com.flux.core.model.AppSettings
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.VibrationPattern
import kotlinx.serialization.Serializable

/** Transport wrapper: protocol version + unique id (idempotency) + payload. */
@Serializable
data class SyncEnvelope(
    val version: Int,
    val id: String,
    val sentAt: Long,
    val message: SyncMessage,
)

/**
 * State the phone publishes through DataClient so the watch can render offline:
 * categories, the agenda (upcoming notes) and watch-relevant settings.
 */
@Serializable
data class WatchSnapshot(
    val version: Int,
    val generatedAt: Long,
    val accountState: AccountState,
    val linkedAccountId: String?,
    val userName: String?,
    val categories: List<Category>,
    val agenda: List<Note>,
    val snoozeMinutes: Int,
    val vibration: VibrationPattern,
    val quietHours: QuietWindow?,
    /** v1.1 (additive, older phones omit it): how long the watch vibrates for a reminder. */
    val alarmDurationSeconds: Int = AppSettings.DEFAULT_ALARM_SECONDS,
    /** v1.1: versionCode of the phone app, to warn when the two apps differ. */
    val phoneAppVersion: Int = 0,
)

@Serializable
data class QuietWindow(val startMinutes: Int, val endMinutes: Int)
