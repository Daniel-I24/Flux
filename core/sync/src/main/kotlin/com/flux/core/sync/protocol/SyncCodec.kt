package com.flux.core.sync.protocol

import javax.inject.Inject
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface DecodeResult<out T> {
    data class Valid<T>(val value: T) : DecodeResult<T>

    /** The other app speaks another protocol version: ask the user to update. */
    data class UnsupportedVersion(val remoteVersion: Int) : DecodeResult<Nothing>
    data class Invalid(val reason: String) : DecodeResult<Nothing>
}

/** Safe (de)serialization of everything received through the Data Layer. */
class SyncCodec @Inject constructor() {
    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(envelope: SyncEnvelope): ByteArray = json.encodeToString(SyncEnvelope.serializer(), envelope).toByteArray()

    fun encode(snapshot: WatchSnapshot): ByteArray = json.encodeToString(WatchSnapshot.serializer(), snapshot).toByteArray()

    fun decodeEnvelope(bytes: ByteArray): DecodeResult<SyncEnvelope> {
        if (bytes.size > SyncProtocol.MAX_MESSAGE_BYTES) return DecodeResult.Invalid("too large")
        val raw = bytes.decodeToString()
        versionOf(raw)?.let { if (it != SyncProtocol.VERSION) return DecodeResult.UnsupportedVersion(it) }
        val envelope = parse { json.decodeFromString(SyncEnvelope.serializer(), raw) } ?: return DecodeResult.Invalid("malformed")
        return if (MessageValidator.isValid(envelope.message)) DecodeResult.Valid(envelope) else DecodeResult.Invalid("invalid fields")
    }

    fun decodeSnapshot(bytes: ByteArray): DecodeResult<WatchSnapshot> {
        if (bytes.size > SyncProtocol.MAX_SNAPSHOT_BYTES) return DecodeResult.Invalid("too large")
        val raw = bytes.decodeToString()
        versionOf(raw)?.let { if (it != SyncProtocol.VERSION) return DecodeResult.UnsupportedVersion(it) }
        val snapshot = parse { json.decodeFromString(WatchSnapshot.serializer(), raw) } ?: return DecodeResult.Invalid("malformed")
        return if (MessageValidator.isValid(snapshot)) DecodeResult.Valid(snapshot) else DecodeResult.Invalid("invalid fields")
    }

    private fun versionOf(raw: String): Int? = parse { json.parseToJsonElement(raw).jsonObject["version"]?.jsonPrimitive?.int }

    private inline fun <T> parse(block: () -> T): T? = try {
        block()
    } catch (_: SerializationException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
