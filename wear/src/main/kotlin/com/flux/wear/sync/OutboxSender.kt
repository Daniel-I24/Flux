package com.flux.wear.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.flux.core.data.repository.OutboxRepository
import com.flux.core.model.FluxResult
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.SyncProtocol
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Schedules delivery of queued captures; WorkManager retries with backoff while the phone is away. */
class OutboxSender @Inject constructor(@ApplicationContext private val context: Context) {
    fun trigger() {
        val request = OneTimeWorkRequestBuilder<SendOutboxWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private companion object {
        const val WORK_NAME = "flux-outbox"
        const val BACKOFF_SECONDS = 15L
    }
}

/**
 * Sends every queued capture to the phone. Items stay in the queue until the phone acks
 * them (NoteAck); re-sending is safe because the phone merges by note id (idempotent).
 */
@HiltWorker
class SendOutboxWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val outbox: OutboxRepository,
    private val gateway: DataLayerGateway,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pending = outbox.pending()
        if (pending.isEmpty()) return Result.success()
        val phone = gateway.nodesWithCapability(SyncProtocol.PHONE_CAPABILITY, reachableOnly = true).firstOrNull()
            ?: return Result.retry()
        var failed = false
        pending.forEach { item ->
            if (gateway.sendRaw(phone.id, item.payload) is FluxResult.Failure) {
                outbox.markFailed(item)
                failed = true
            }
        }
        return if (failed) Result.retry() else Result.success()
    }
}
