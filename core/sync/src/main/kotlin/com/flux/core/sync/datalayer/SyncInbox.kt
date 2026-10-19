package com.flux.core.sync.datalayer

import com.flux.core.sync.protocol.DecodeResult
import com.flux.core.sync.protocol.MessageDeduplicator
import com.flux.core.sync.protocol.SyncCodec
import com.flux.core.sync.protocol.SyncEnvelope
import com.flux.core.sync.protocol.SyncMessage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

data class Incoming(val envelope: SyncEnvelope, val sourceNodeId: String)

/**
 * Single entry point for received bytes: decodes safely, drops duplicates,
 * completes pending request/response pairs and publishes the rest (Observer).
 */
@Singleton
class SyncInbox @Inject constructor(private val codec: SyncCodec) {
    private val deduplicator = MessageDeduplicator()
    private val pending = mutableMapOf<String, CompletableDeferred<SyncMessage>>()
    private val incoming = MutableSharedFlow<Incoming>(extraBufferCapacity = BUFFER)
    private val protocolMismatch = MutableStateFlow(false)

    val messages: SharedFlow<Incoming> = incoming.asSharedFlow()

    /** True once the other app answered with an incompatible protocol version. */
    val versionMismatch: StateFlow<Boolean> = protocolMismatch.asStateFlow()

    fun receive(bytes: ByteArray, sourceNodeId: String): Boolean {
        val envelope = when (val decoded = codec.decodeEnvelope(bytes)) {
            is DecodeResult.Valid -> decoded.value
            is DecodeResult.UnsupportedVersion -> {
                protocolMismatch.value = true
                return false
            }
            is DecodeResult.Invalid -> return false
        }
        protocolMismatch.value = false
        if (!deduplicator.markIfNew(envelope.id)) return false
        val correlationId = envelope.message.correlationId()
        val waiter = correlationId?.let { synchronized(pending) { pending.remove(it) } }
        if (waiter != null) waiter.complete(envelope.message) else incoming.tryEmit(Incoming(envelope, sourceNodeId))
        return true
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
        is SyncMessage.LinkResponse -> "link:$code"
        is SyncMessage.Status -> STATUS_CORRELATION
        else -> null
    }

    companion object {
        const val STATUS_CORRELATION = "status"
        fun linkCorrelation(code: String) = "link:$code"
        private const val BUFFER = 64
    }
}
