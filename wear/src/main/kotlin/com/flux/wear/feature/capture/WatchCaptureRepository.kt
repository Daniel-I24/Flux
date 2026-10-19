package com.flux.wear.feature.capture

import com.flux.core.ai.local.LocalNoteClassifier
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.model.Classification
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.datalayer.SyncInbox
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.SyncMessageFactory
import com.flux.wear.BuildConfig
import com.flux.wear.sync.PhoneConnectionMonitor
import javax.inject.Inject

/**
 * Classification asks the phone first (it has the remote AI, the profile and the user's
 * categories). If the phone is away or slow, the watch classifies locally so the user is
 * never blocked.
 */
class WatchCaptureRepository @Inject constructor(
    private val gateway: DataLayerGateway,
    private val inbox: SyncInbox,
    private val factory: SyncMessageFactory,
    private val monitor: PhoneConnectionMonitor,
    private val localClassifier: LocalNoteClassifier,
    private val categories: CategoryRepository,
    private val saver: WatchNoteSaver,
    private val clock: Clock,
) : CaptureEffects {
    override suspend fun classify(text: String): Classification {
        monitor.readyPhoneNode()?.let { phone ->
            val request = factory.classifyRequest(text)
            val requestId = (request.message as SyncMessage.ClassifyRequest).requestId
            inbox.request<SyncMessage.ClassifyResponse>(requestId, BuildConfig.PHONE_REPLY_TIMEOUT_MS) {
                gateway.send(phone, request) is FluxResult.Success
            }?.let { return it.classification }
        }
        val available = categories.getCategories().ifEmpty { DefaultCategory.all() }
        val local = localClassifier.classify(ClassificationRequest(text, clock.now(), clock.zone(), available, profile = null))
        return (local as FluxResult.Success).value
    }

    override suspend fun save(text: String, classification: Classification, corrected: Boolean): SaveResult =
        SaveResult(note = saver.save(text, classification, corrected), queued = monitor.readyPhoneNode() == null)
}
