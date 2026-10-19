package com.flux.core.ai.remote

import com.flux.core.ai.date.SpanishDateParser
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.model.AppError
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import com.flux.core.model.Priority
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.io.InterruptedIOException
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

/**
 * Remote strategy: our server calls Gemini (the API key never reaches the device).
 * Any failure (no session, timeout, 5xx, invalid answer) returns a Failure so the
 * chain falls back to the local classifier. The deterministic date parser wins over
 * the model's date arithmetic.
 */
class RemoteNoteClassifier(
    baseClient: OkHttpClient,
    private val config: RemoteClassifierConfig,
    private val credentials: RemoteCredentials,
    private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false },
) : NoteClassifier {
    override val engine = ClassifierEngine.REMOTE

    private val client = baseClient.newBuilder().callTimeout(config.timeoutMs, TimeUnit.MILLISECONDS).build()

    override suspend fun classify(request: ClassificationRequest): FluxResult<Classification> {
        if (config.baseUrl.isBlank()) return FluxResult.Failure(AppError.NotConfigured)
        val idToken = credentials.idToken() ?: return FluxResult.Failure(AppError.Unauthorized)
        val body = json.encodeToString(RemoteClassifyRequest.serializer(), request.toRemote())
        val httpRequest = Request.Builder()
            .url(config.baseUrl.trimEnd('/') + CLASSIFY_PATH)
            .header("Authorization", "Bearer $idToken")
            .apply { credentials.appCheckToken()?.let { header("X-Firebase-AppCheck", it) } }
            .post(body.toRequestBody(JSON_MEDIA))
            .build()
        return try {
            client.newCall(httpRequest).await().use { response ->
                when {
                    response.code == HTTP_UNAUTHORIZED || response.code == HTTP_FORBIDDEN -> FluxResult.Failure(AppError.Unauthorized)
                    !response.isSuccessful -> FluxResult.Failure(AppError.ServerUnavailable)
                    else -> parse(response.body?.string().orEmpty(), request)
                }
            }
        } catch (_: InterruptedIOException) {
            FluxResult.Failure(AppError.Timeout)
        } catch (_: IOException) {
            FluxResult.Failure(AppError.Network)
        }
    }

    private fun parse(raw: String, request: ClassificationRequest): FluxResult<Classification> {
        val answer = try {
            json.decodeFromString(RemoteClassifyResponse.serializer(), raw)
        } catch (_: SerializationException) {
            return FluxResult.Failure(AppError.InvalidInput)
        } catch (_: IllegalArgumentException) {
            return FluxResult.Failure(AppError.InvalidInput)
        }
        val validCategory = request.categories.any { it.id == answer.categoryId }
        val title = answer.title.trim().take(Note.MAX_TITLE_LENGTH)
        if (!validCategory || title.isEmpty()) return FluxResult.Failure(AppError.InvalidInput)

        val now = Instant.ofEpochMilli(request.nowMillis).atZone(request.zone)
        val deterministic = SpanishDateParser(request.profile?.routine).parse(request.text, now)
        val modelDue = answer.dueAt?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() }
        return FluxResult.Success(
            Classification(
                categoryId = answer.categoryId,
                title = title,
                dueAt = deterministic?.dateTime?.toInstant()?.toEpochMilli() ?: modelDue,
                priority = Priority.entries.firstOrNull { it.name == answer.priority.uppercase() } ?: Priority.NORMAL,
                requiresAlarm = answer.requiresAlarm,
                confidence = answer.confidence.coerceIn(0.0, 1.0),
                engine = engine,
            ),
        )
    }

    private companion object {
        const val CLASSIFY_PATH = "/api/classify"
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
