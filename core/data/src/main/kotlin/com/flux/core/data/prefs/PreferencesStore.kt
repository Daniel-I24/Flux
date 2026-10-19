package com.flux.core.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Typed JSON values on top of Preferences DataStore. Values marked sensitive are
 * encrypted with an Android Keystore key; corrupted values read as null.
 */
@Singleton
class PreferencesStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val cipher: DataCipher,
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun <T> observe(key: String, serializer: KSerializer<T>, sensitive: Boolean = false): Flow<T?> =
        dataStore.data.map { prefs -> prefs[stringPreferencesKey(key)]?.let { decode(it, serializer, sensitive) } }.distinctUntilChanged()

    suspend fun <T> get(key: String, serializer: KSerializer<T>, sensitive: Boolean = false): T? =
        observe(key, serializer, sensitive).first()

    suspend fun <T> put(key: String, serializer: KSerializer<T>, value: T, sensitive: Boolean = false) {
        val encoded = json.encodeToString(serializer, value).let { if (sensitive) cipher.encrypt(it) else it }
        dataStore.edit { it[stringPreferencesKey(key)] = encoded }
    }

    suspend fun remove(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey(key)) }
    }

    private fun <T> decode(raw: String, serializer: KSerializer<T>, sensitive: Boolean): T? = try {
        val plain = if (sensitive) cipher.decrypt(raw) ?: return null else raw
        json.decodeFromString(serializer, plain)
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
