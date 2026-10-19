package com.flux.wear.data

import com.flux.core.data.prefs.PreferencesStore
import com.flux.core.model.AppSettings
import com.flux.core.model.BundledSound
import com.flux.core.model.SoundRef
import com.flux.core.model.VibrationPattern
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.QuietWindow
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

/** Last state published by the phone (kept so the watch can work offline). */
@Serializable
data class PhoneState(
    val accountState: AccountState = AccountState.SIGNED_OUT,
    val linkedAccountId: String? = null,
    val userName: String? = null,
    val snoozeMinutes: Int = AppSettings.DEFAULT_SNOOZE_MINUTES,
    val quietHours: QuietWindow? = null,
    val receivedAt: Long = 0,
)

@Serializable
enum class WatchTheme { DEFAULT, HIGH_CONTRAST }

/** Preferences owned by the watch itself (Ajustes del reloj). */
@Serializable
data class WatchSettings(
    val vibration: VibrationPattern = VibrationPattern.DOUBLE,
    val soundEnabled: Boolean = true,
    val sound: SoundRef = SoundRef.Bundled(BundledSound.CHIME),
    val theme: WatchTheme = WatchTheme.DEFAULT,
)

@Singleton
class WatchStateStore @Inject constructor(private val store: PreferencesStore) {
    fun observePhoneState(): Flow<PhoneState> = store.observe(PHONE_STATE, PhoneState.serializer()).map { it ?: PhoneState() }

    suspend fun phoneState(): PhoneState = observePhoneState().first()

    suspend fun savePhoneState(state: PhoneState) = store.put(PHONE_STATE, PhoneState.serializer(), state)

    /** Account this watch accepted (set only after the user confirmed the code on the watch). */
    fun observeLinkedAccount(): Flow<String?> = store.observe(LINKED_ACCOUNT, String.serializer())

    suspend fun linkedAccount(): String? = observeLinkedAccount().first()

    suspend fun setLinkedAccount(accountId: String?) {
        if (accountId == null) store.remove(LINKED_ACCOUNT) else store.put(LINKED_ACCOUNT, String.serializer(), accountId)
    }

    fun observeSettings(): Flow<WatchSettings> = store.observe(SETTINGS, WatchSettings.serializer()).map { it ?: WatchSettings() }

    suspend fun settings(): WatchSettings = observeSettings().first()

    suspend fun updateSettings(transform: (WatchSettings) -> WatchSettings) = store.put(
        SETTINGS,
        WatchSettings.serializer(),
        transform(settings())
    )

    private companion object {
        const val PHONE_STATE = "watch_phone_state"
        const val LINKED_ACCOUNT = "watch_linked_account"
        const val SETTINGS = "watch_settings"
    }
}
