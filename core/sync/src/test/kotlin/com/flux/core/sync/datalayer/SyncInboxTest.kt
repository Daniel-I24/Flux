package com.flux.core.sync.datalayer

import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.sync.protocol.SyncCodec
import com.flux.core.sync.protocol.SyncEnvelope
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncProtocol
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncInboxTest {
    private val codec = SyncCodec()
    private val inbox = SyncInbox(codec)

    private fun bytes(id: String, message: SyncMessage) = codec.encode(SyncEnvelope(SyncProtocol.VERSION, id, 1L, message))

    @Test
    fun `returns new messages once`() {
        assertEquals(SyncMessage.NoteAck("n1"), inbox.receive(bytes("e1", SyncMessage.NoteAck("n1")), "node")?.envelope?.message)
        assertNull(inbox.receive(bytes("e1", SyncMessage.NoteAck("n1")), "node"))
        assertNull(inbox.receive("garbage".toByteArray(), "node"))
    }

    @Test
    fun `completes a pending request with its correlated reply`() = runTest {
        val classification = Classification("tasks", "x", confidence = 1.0, engine = ClassifierEngine.REMOTE)
        val reply = async {
            inbox.request<SyncMessage.ClassifyResponse>("req-1", timeoutMs = 5_000) { true }
        }
        yield()
        inbox.receive(bytes("e2", SyncMessage.ClassifyResponse("req-1", classification)), "phone")
        assertEquals(classification, reply.await()?.classification)
    }

    @Test
    fun `returns null on timeout or when sending fails`() = runTest {
        assertNull(inbox.request<SyncMessage.ClassifyResponse>("req-2", timeoutMs = 100) { true })
        assertNull(inbox.request<SyncMessage.ClassifyResponse>("req-3", timeoutMs = 100) { false })
    }

    @Test
    fun `flags protocol mismatches`() {
        val other = """{"version":2,"id":"x","sentAt":1,"message":{"type":"note_ack","noteId":"a"}}"""
        inbox.receive(other.toByteArray(), "node")
        assertTrue(inbox.versionMismatch.value)
    }
}
