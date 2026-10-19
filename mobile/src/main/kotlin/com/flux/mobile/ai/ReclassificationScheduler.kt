package com.flux.mobile.ai

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.flux.core.domain.port.ReclassificationQueue
import com.flux.core.domain.usecase.ReclassifyNoteUseCase
import com.flux.core.domain.usecase.ReclassifyOutcome
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/** Queues a remote reclassification that only runs with internet (no polling). */
class ReclassificationScheduler @Inject constructor(@ApplicationContext private val context: Context) : ReclassificationQueue {
    override fun enqueue(noteId: String) {
        val request = OneTimeWorkRequestBuilder<ReclassifyWorker>()
            .setInputData(workDataOf(ReclassifyWorker.KEY_NOTE_ID to noteId))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("reclassify-$noteId", ExistingWorkPolicy.KEEP, request)
    }

    private companion object {
        const val BACKOFF_SECONDS = 30L
    }
}

@HiltWorker
class ReclassifyWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val reclassify: ReclassifyNoteUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val noteId = inputData.getString(KEY_NOTE_ID) ?: return Result.failure()
        if (runAttemptCount >= MAX_ATTEMPTS) return Result.success()
        return when (reclassify(noteId)) {
            ReclassifyOutcome.RETRY_LATER -> Result.retry()
            ReclassifyOutcome.UPDATED, ReclassifyOutcome.SKIPPED -> Result.success()
        }
    }

    companion object {
        const val KEY_NOTE_ID = "noteId"
        private const val MAX_ATTEMPTS = 6
    }
}
