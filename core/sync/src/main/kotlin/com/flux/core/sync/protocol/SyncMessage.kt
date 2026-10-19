package com.flux.core.sync.protocol

import com.flux.core.model.Classification
import com.flux.core.model.Note
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccountState { SIGNED_OUT, ONBOARDING, READY }

@Serializable
enum class WatchOwnership { LINKED_HERE, NOT_LINKED, LINKED_TO_OTHER }

@Serializable
enum class NoteActionType { COMPLETE, SNOOZE }

/**
 * Every message exchanged through MessageClient. Polymorphic JSON with a "type"
 * discriminator; unknown types or invalid fields are discarded by [SyncCodec].
 */
@Serializable
sealed interface SyncMessage {
    /** Watch → phone on start and on reconnection. [linkedAccountId] is what the watch stored. */
    @Serializable
    @SerialName("hello")
    data class Hello(val appVersion: Int, val linkedAccountId: String?) : SyncMessage

    /** Phone → watch: account and link state, answer to [Hello]. */
    @Serializable
    @SerialName("status")
    data class Status(
        val accountState: AccountState,
        val ownership: WatchOwnership,
        val userName: String? = null,
        val serverReachable: Boolean = false,
    ) : SyncMessage

    @Serializable
    @SerialName("link_request")
    data class LinkRequest(val code: String, val accountId: String, val userName: String?) : SyncMessage

    @Serializable
    @SerialName("link_response")
    data class LinkResponse(val code: String, val accepted: Boolean, val ownerAccountId: String? = null) : SyncMessage

    @Serializable
    @SerialName("unlink")
    data class Unlink(val accountId: String) : SyncMessage

    @Serializable
    @SerialName("classify_request")
    data class ClassifyRequest(val requestId: String, val text: String) : SyncMessage

    @Serializable
    @SerialName("classify_response")
    data class ClassifyResponse(val requestId: String, val classification: Classification) : SyncMessage

    /** Watch → phone: a captured note. Idempotent by [Note.id]. */
    @Serializable
    @SerialName("note_capture")
    data class NoteCapture(val note: Note) : SyncMessage

    @Serializable
    @SerialName("note_ack")
    data class NoteAck(val noteId: String) : SyncMessage

    /** Either direction: user completed or snoozed a reminder on one device. */
    @Serializable
    @SerialName("note_action")
    data class NoteAction(val noteId: String, val action: NoteActionType, val snoozeMinutes: Int? = null) : SyncMessage
}
