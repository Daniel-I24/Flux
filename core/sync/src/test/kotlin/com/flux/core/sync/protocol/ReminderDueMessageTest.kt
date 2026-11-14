package com.flux.core.sync.protocol

import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderDueMessageTest {
    private val codec = SyncCodec()
    private val factory = SyncMessageFactory(
        object : Clock {
            override fun now() = 1_000L
            override fun zone(): ZoneId = ZoneId.of("UTC")
        },
        IdGenerator { "env-1" },
    )

    @Test
    fun `the phone's ringing message round trips with its own type`() {
        val envelope = factory.reminderDue("note-1")
        val bytes = codec.encode(envelope)
        assertTrue(bytes.decodeToString().contains("\"type\":\"reminder_due\""))
        assertEquals(DecodeResult.Valid(envelope), codec.decodeEnvelope(bytes))
    }

    @Test
    fun `a ringing message without a note id is discarded`() {
        val bytes = codec.encode(factory.envelope(SyncMessage.ReminderDue(" ")))
        assertTrue(codec.decodeEnvelope(bytes) !is DecodeResult.Valid)
    }
}
