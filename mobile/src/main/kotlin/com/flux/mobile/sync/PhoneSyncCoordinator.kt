package com.flux.mobile.sync

import com.flux.core.domain.usecase.ClassifyNoteUseCase
import com.flux.core.domain.usecase.CurrentAccountIdUseCase
import com.flux.core.domain.usecase.UnlinkWatchUseCase
import com.flux.core.model.FluxResult
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.datalayer.Incoming
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import javax.inject.Inject
import javax.inject.Singleton

/** Phone side of the protocol: answers the watch (status, classification, link changes). */
@Singleton
class PhoneSyncCoordinator @Inject constructor(
    private val gateway: DataLayerGateway,
    private val factory: SyncMessageFactory,
    private val statusProvider: PhoneStatusProvider,
    private val classify: ClassifyNoteUseCase,
    private val currentAccountId: CurrentAccountIdUseCase,
    private val unlinkWatch: UnlinkWatchUseCase,
    private val noteHandler: PhoneNoteMessageHandler,
) {
    suspend fun handle(incoming: Incoming) {
        val node = incoming.sourceNodeId
        when (val message = incoming.envelope.message) {
            is SyncMessage.Hello -> reply(node, statusProvider.statusFor(message.linkedAccountId))
            is SyncMessage.ClassifyRequest -> when (val result = classify(message.text)) {
                is FluxResult.Success -> reply(node, SyncMessage.ClassifyResponse(message.requestId, result.value))
                // No answer: the watch times out and classifies locally.
                is FluxResult.Failure -> Unit
            }
            is SyncMessage.Unlink -> if (message.accountId == currentAccountId()) unlinkWatch()
            is SyncMessage.NoteCapture, is SyncMessage.NoteAction -> noteHandler.handle(node, message)
            else -> Unit
        }
    }

    private suspend fun reply(nodeId: String, message: SyncMessage) {
        gateway.send(nodeId, factory.envelope(message))
    }
}
