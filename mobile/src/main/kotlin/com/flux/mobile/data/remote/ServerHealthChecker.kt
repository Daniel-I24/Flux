package com.flux.mobile.data.remote

import com.flux.mobile.config.ServerConfig
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** GET /api/health with a short timeout; cached so it never adds latency to every message. */
@Singleton
class ServerHealthChecker @Inject constructor(baseClient: OkHttpClient, private val config: ServerConfig) {
    private val client = baseClient.newBuilder().callTimeout(HEALTH_TIMEOUT_MS, TimeUnit.MILLISECONDS).build()
    private var cached: Pair<Long, Boolean>? = null

    suspend fun isReachable(now: Long = System.currentTimeMillis()): Boolean {
        cached?.let { (checkedAt, value) -> if (now - checkedAt < CACHE_MS) return value }
        if (config.baseUrl.isBlank()) return false
        val reachable = withContext(Dispatchers.IO) {
            try {
                client.newCall(Request.Builder().url(config.baseUrl.trimEnd('/') + HEALTH_PATH).build()).execute().use { it.isSuccessful }
            } catch (_: IOException) {
                false
            }
        }
        cached = now to reachable
        return reachable
    }

    private companion object {
        const val HEALTH_PATH = "/api/health"
        const val HEALTH_TIMEOUT_MS = 3_000L
        const val CACHE_MS = 60_000L
    }
}
