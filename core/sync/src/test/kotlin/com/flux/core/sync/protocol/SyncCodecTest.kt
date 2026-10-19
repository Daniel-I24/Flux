package com.flux.core.sync.protocol

import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.IdGenerator
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import com.flux.core.model.VibrationPattern
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncCodecTest {
    private val codec = SyncCodec()
    private val factory = SyncMessageFactory(
        object : Clock {
            override fun now() = 1_000L
            override fun zone(): ZoneId = ZoneId.of("UTC")
        },
        IdGenerator { "env-1" },
    )
    private val note = Note(
        id = "note-1",
        text = "Recuérdame tomar agua",
        title = "Tomar agua",
        categoryId = "reminders",
        dueAt = 2_000L,
        requiresAlarm = true,
        source = DeviceSource.WATCH,
        engine = ClassifierEngine.LOCAL,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    @Test
    fun `round trips every message through JSON`() {
        val messages = listOf(
            factory.hello(appVersion = 1, linkedAccountId = "uid-hash"),
            factory.noteCapture(note),
            factory.snooze("note-1", 5),
            factory.envelope(SyncMessage.LinkRequest(code = "012345", accountId = "acc", userName = "Ana")),
            factory.envelope(SyncMessage.Status(AccountState.READY, WatchOwnership.LINKED_HERE, "Ana", serverReachable = true)),
        )
        messages.forEach { envelope ->
            val decoded = codec.decodeEnvelope(codec.encode(envelope))
            assertEquals(DecodeResult.Valid(envelope), decoded)
        }
    }

    @Test
    fun `uses a readable type discriminator`() {
        val json = codec.encode(factory.noteAck("note-1")).decodeToString()
        assertTrue(json.contains("\"type\":\"note_ack\""))
        assertTrue(json.contains("\"version\":${SyncProtocol.VERSION}"))
    }

    @Test
    fun `reports a different protocol version`() {
        val json = """{"version":99,"id":"x","sentAt":1,"message":{"type":"note_ack","noteId":"a"}}"""
        assertEquals(DecodeResult.UnsupportedVersion(99), codec.decodeEnvelope(json.toByteArray()))
    }

    @Test
    fun `discards malformed, unknown and oversized messages`() {
        assertTrue(codec.decodeEnvelope("not json".toByteArray()) is DecodeResult.Invalid)
        val unknown = """{"version":1,"id":"x","sentAt":1,"message":{"type":"format_disk"}}"""
        assertTrue(codec.decodeEnvelope(unknown.toByteArray()) is DecodeResult.Invalid)
        assertTrue(codec.decodeEnvelope(ByteArray(SyncProtocol.MAX_MESSAGE_BYTES + 1)) is DecodeResult.Invalid)
    }

    @Test
    fun `validates fields after decoding`() {
        val badCode = factory.envelope(SyncMessage.LinkRequest(code = "12ab", accountId = "acc", userName = null))
        assertTrue(codec.decodeEnvelope(codec.encode(badCode)) is DecodeResult.Invalid)
        val emptyText = factory.envelope(SyncMessage.ClassifyRequest(requestId = "r", text = "  "))
        assertTrue(codec.decodeEnvelope(codec.encode(emptyText)) is DecodeResult.Invalid)
        val hugeSnooze = factory.envelope(SyncMessage.NoteAction("n", NoteActionType.SNOOZE, snoozeMinutes = 100_000))
        assertTrue(codec.decodeEnvelope(codec.encode(hugeSnooze)) is DecodeResult.Invalid)
    }

    @Test
    fun `round trips the watch snapshot`() {
        val snapshot = WatchSnapshot(
            version = SyncProtocol.VERSION,
            generatedAt = 1L,
            accountState = AccountState.READY,
            linkedAccountId = "acc",
            userName = "Ana",
            categories = DefaultCategory.all(),
            agenda = listOf(note),
            snoozeMinutes = 10,
            vibration = VibrationPattern.LONG,
            quietHours = QuietWindow(22 * 60, 6 * 60),
        )
        assertEquals(DecodeResult.Valid(snapshot), codec.decodeSnapshot(codec.encode(snapshot)))
    }

    @Test
    fun `deduplicator accepts each id once and stays bounded`() {
        val deduplicator = MessageDeduplicator(capacity = 2)
        assertTrue(deduplicator.markIfNew("a"))
        assertFalse(deduplicator.markIfNew("a"))
        deduplicator.markIfNew("b")
        deduplicator.markIfNew("c")
        assertTrue(deduplicator.markIfNew("a"))
    }
}
