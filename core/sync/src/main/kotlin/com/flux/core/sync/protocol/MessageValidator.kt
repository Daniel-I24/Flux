package com.flux.core.sync.protocol

import com.flux.core.model.Category
import com.flux.core.model.Note

/** Field-level rules applied after deserialization; a message that breaks any is discarded. */
internal object MessageValidator {
    private const val MAX_ID_LENGTH = 64
    private const val MAX_NAME_LENGTH = 60
    private const val MAX_SNOOZE_MINUTES = 24 * 60
    private val CODE = Regex("^\\d{${SyncProtocol.LINK_CODE_LENGTH}}$")

    fun isValid(message: SyncMessage): Boolean = when (message) {
        is SyncMessage.Hello -> message.appVersion > 0 && message.linkedAccountId.isValidIdOrNull()
        is SyncMessage.Status -> message.userName.isShortOrNull()
        is SyncMessage.LinkRequest -> CODE.matches(message.code) && message.accountId.isValidId() && message.userName.isShortOrNull()
        is SyncMessage.LinkResponse -> CODE.matches(message.code) && message.ownerAccountId.isValidIdOrNull()
        is SyncMessage.Unlink -> message.accountId.isValidId()
        is SyncMessage.ClassifyRequest -> message.requestId.isValidId() &&
            message.text.isNotBlank() &&
            message.text.length <= Note.MAX_TEXT_LENGTH
        is SyncMessage.ClassifyResponse -> message.requestId.isValidId() && message.classification.title.length <= Note.MAX_TITLE_LENGTH
        is SyncMessage.NoteCapture -> message.note.isValid()
        is SyncMessage.NoteAck -> message.noteId.isValidId()
        is SyncMessage.NoteAction -> message.noteId.isValidId() && (message.snoozeMinutes ?: 1) in 1..MAX_SNOOZE_MINUTES
    }

    fun isValid(snapshot: WatchSnapshot): Boolean =
        snapshot.agenda.all { it.isValid() } && snapshot.categories.all { it.isValid() } && snapshot.linkedAccountId.isValidIdOrNull()

    private fun Note.isValid(): Boolean = id.isValidId() &&
        text.isNotBlank() &&
        text.length <= Note.MAX_TEXT_LENGTH &&
        title.isNotBlank() &&
        title.length <= Note.MAX_TITLE_LENGTH &&
        categoryId.isValidId() &&
        updatedAt >= createdAt

    private fun Category.isValid(): Boolean = id.isValidId() && (customName?.length ?: 0) <= Category.MAX_NAME_LENGTH

    private fun String.isValidId() = isNotBlank() && length <= MAX_ID_LENGTH

    private fun String?.isValidIdOrNull() = this == null || isValidId()

    private fun String?.isShortOrNull() = this == null || length <= MAX_NAME_LENGTH
}
