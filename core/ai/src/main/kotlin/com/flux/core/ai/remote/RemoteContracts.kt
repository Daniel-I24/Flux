package com.flux.core.ai.remote

import kotlinx.serialization.Serializable

/** POST /api/classify body. Default categories travel by id; custom ones with their name. */
@Serializable
internal data class RemoteClassifyRequest(
    val text: String,
    val now: String,
    val timeZone: String,
    val categories: List<RemoteCategory>,
    val profile: RemoteProfile? = null,
)

@Serializable
internal data class RemoteCategory(val id: String, val name: String? = null)

@Serializable
internal data class RemoteProfile(
    val occupation: String,
    val wakeUp: String,
    val sleep: String,
    val breakfast: String,
    val lunch: String,
    val dinner: String,
    val activityEndToday: String? = null,
)

@Serializable
internal data class RemoteClassifyResponse(
    val categoryId: String,
    val title: String,
    val dueAt: String? = null,
    val priority: String = "NORMAL",
    val requiresAlarm: Boolean = false,
    val confidence: Double = 0.5,
)

/** Server configuration injected from BuildConfig (never hard-coded). */
data class RemoteClassifierConfig(val baseUrl: String, val timeoutMs: Long)

/** Firebase ID token and App Check token providers (adapters live in the phone app). */
interface RemoteCredentials {
    suspend fun idToken(): String?
    suspend fun appCheckToken(): String?
}
