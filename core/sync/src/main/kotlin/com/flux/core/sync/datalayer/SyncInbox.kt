package com.flux.core.sync.datalayer

import com.flux.core.sync.protocol.DecodeResult
import com.flux.core.sync.protocol.MessageDeduplicator
import com.flux.core.sync.protocol.SyncCodec
import com.flux.core.sync.protocol.SyncEnvelope
import com.flux.core.sync.protocol.SyncMessage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

data class Incoming(val envelope: SyncEnvelope, val sourceNodeId: String)

/**
 * Single entry point for received bytes: decodes safely, drops duplicates and
 * completes pending request/response pairs. Anything else is returned to the caller
 * (the WearableListenerService), so no message is lost while the app is starting.
 */
@Singleton
class SyncInbox @Inject constructor(private val codec: SyncCodec) {
    private val deduplicator = MessageDeduplicator()
    private val pending = mutableMapOf<String, CompletableDeferred<SyncMessage>>()
    private val protocolMismatch = MutableStateFlow(false)

    /** True once the other app sent an incompatible protocol version. */
    val versionMismatch: StateFlow<Boolean> = protocolMismatch.asStateFlow()

    /** Returns the message to handle, or null if it was invalid, duplicated or an awaited reply. */
    fun receive(bytes: ByteArray, sourceNodeId: String): Incoming? {
        val envelope = when (val decoded = codec.decodeEnvelope(bytes)) {
            is DecodeResult.Valid -> decoded.value
            is DecodeResult.UnsupportedVersion -> {
                protocolMismatch.value = true
                return null
            }
            is DecodeResult.Invalid -> return null
        }
        protocolMismatch.value = false
        if (!deduplicator.markIfNew(envelope.id)) return null
        val waiter = envelope.message.correlationId()?.let { id -> synchronized(pending) { pending.remove(id) } }
        if (waiter != null) {
            waiter.complete(envelope.message)
            return null
        }
        return Incoming(envelope, sourceNodeId)
    }

    /** Sends via [send] and waits for the reply that carries the same correlation id. */
    suspend fun <T : SyncMessage> request(correlationId: String, timeoutMs: Long, send: suspend () -> Boolean): T? {
        val deferred = CompletableDeferred<SyncMessage>()
        synchronized(pending) { pending[correlationId] = deferred }
        return try {
            if (!send()) return null
            @Suppress("UNCHECKED_CAST")
            withTimeoutOrNull(timeoutMs) { deferred.await() } as? T
        } finally {
            synchronized(pending) { pending.remove(correlationId) }
        }
    }

    private fun SyncMessage.correlationId(): String? = when (this) {
        is SyncMessage.ClassifyResponse -> requestId
        is SyncMessage.LinkResponse -> linkCorrelation(code)
        is SyncMessage.Status -> STATUS_CORRELATION
        else -> null
    }

    companion object {
        const val STATUS_CORRELATION = "status"
        fun linkCorrelation(code: String) = "link:$code"
    }
}
