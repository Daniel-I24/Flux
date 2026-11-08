package com.flux.mobile.data.remote

import android.util.Log
import com.flux.core.ai.remote.RemoteCredentials
import com.flux.core.domain.port.RemoteAiState
import com.flux.core.domain.port.RemoteAiStatus
import com.flux.mobile.config.ServerConfig
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * GET /api/health. The quick check gives up after 3 s, which a server waking from sleep
 * (about 50 s on the free hosting) always exceeds: that is reported as WAKING, not as offline,
 * and the patient check waits for the cold start. Only successes are cached, so a server
 * that was asleep is never reported as unreachable for a whole minute.
 */
@Singleton
class ServerHealthChecker @Inject constructor(
    baseClient: OkHttpClient,
    private val config: ServerConfig,
    private val credentials: RemoteCredentials,
) : RemoteAiStatus {
    private val quickClient = baseClient.newBuilder().callTimeout(QUICK_TIMEOUT_MS, TimeUnit.MILLISECONDS).build()
    private val patientClient = baseClient.newBuilder()
        .callTimeout(WAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .readTimeout(WAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        .build()

    @Volatile private var lastSuccess = 0L

    @Volatile private var lastFailure = 0L

    private enum class Probe { OK, SLOW, FAILED }

    override suspend fun check(patient: Boolean): RemoteAiState {
        if (config.baseUrl.isBlank()) return RemoteAiState.NOT_CONFIGURED.also { log("not configured") }
        if (credentials.idToken() == null) return RemoteAiState.NO_SESSION.also { log("no session token") }
        val started = System.currentTimeMillis()
        val probe = if (patient || !isFresh(started)) ping(if (patient) patientClient else quickClient) else Probe.OK
        val state = when (probe) {
            Probe.OK -> RemoteAiState.AVAILABLE
            Probe.SLOW -> if (patient) RemoteAiState.OFFLINE else RemoteAiState.WAKING
            Probe.FAILED -> RemoteAiState.OFFLINE
        }
        log("check patient=$patient state=$state ms=${System.currentTimeMillis() - started}")
        return state
    }

    /** For the watch's status: a quick answer that never waits for a cold start; a failure is remembered for a minute. */
    suspend fun isReachable(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastFailure < CACHE_MS) return false
        return (check(patient = false) == RemoteAiState.AVAILABLE).also { if (!it) lastFailure = now }
    }

    private fun isFresh(now: Long) = now - lastSuccess < CACHE_MS

    /** A 5xx while the hosting starts the server counts as slow, like a timeout. */
    private suspend fun ping(client: OkHttpClient): Probe = withContext(Dispatchers.IO) {
        try {
            client.newCall(Request.Builder().url(config.baseUrl.trimEnd('/') + HEALTH_PATH).build()).execute().use { response ->
                when {
                    response.isSuccessful -> Probe.OK.also {
                        lastSuccess = System.currentTimeMillis()
                        lastFailure = 0L
                    }
                    response.code >= HTTP_SERVER_ERROR -> Probe.SLOW
                    else -> Probe.FAILED
                }
            }
        } catch (_: InterruptedIOException) {
            Probe.SLOW
        } catch (_: IOException) {
            Probe.FAILED
        }
    }

    /** States and durations only: never the URL, tokens or account data. */
    private fun log(message: String) {
        Log.println(Log.INFO, TAG, message)
    }

    private companion object {
        const val TAG = "FluxAi"
        const val HEALTH_PATH = "/api/health"
        const val QUICK_TIMEOUT_MS = 3_000L
        const val WAKE_TIMEOUT_MS = 60_000L
        const val CACHE_MS = 60_000L
        const val HTTP_SERVER_ERROR = 500
    }
}
