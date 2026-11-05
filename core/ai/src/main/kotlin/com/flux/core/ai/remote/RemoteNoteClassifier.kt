package com.flux.core.ai.remote

import com.flux.core.ai.date.SpanishDateParser
import com.flux.core.ai.local.TitleBuilder
import com.flux.core.ai.local.TitleRules
import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.model.Ambiguity
import com.flux.core.model.AppError
import com.flux.core.model.Classification
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.FluxResult
import com.flux.core.model.Note
import com.flux.core.model.Priority
import java.io.IOException
import java.io.InterruptedIOException
import java.time.Instant
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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
    private val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    },
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
        val categoryId = request.categoryIdOf(answer.categoryId) ?: return FluxResult.Failure(AppError.InvalidInput)
        if (answer.title.isBlank()) return FluxResult.Failure(AppError.InvalidInput)

        val now = Instant.ofEpochMilli(request.nowMillis).atZone(request.zone)
        // The text wins: a time the user said is never replaced by the model's (or the profile's) guess.
        val deterministic = SpanishDateParser(request.profile?.routine).parse(request.text, now)
        val modelDue = answer.dueAt?.let { runCatching { OffsetDateTime.parse(it).toInstant().toEpochMilli() }.getOrNull() }
        val title = answer.title.trim().take(Note.MAX_TITLE_LENGTH).takeIf(TitleRules::isMeaningful)
            ?: TitleBuilder.build(request.text, deterministic?.fragments.orEmpty())
        val alternatives = answer.alternativeCategoryIds.mapNotNull { request.categoryIdOf(it) }.filter { it != categoryId }.distinct()
        val ambiguities = buildList {
            deterministic?.alternatives?.takeIf { it.size > 1 }?.let { options ->
                add(Ambiguity.DueTime(options.map { it.toInstant().toEpochMilli() }))
            }
            alternatives.firstOrNull()?.let { add(Ambiguity.CategoryChoice(listOf(categoryId, it))) }
        }
        return FluxResult.Success(
            Classification(
                categoryId = categoryId,
                title = title,
                dueAt = deterministic?.dateTime?.toInstant()?.toEpochMilli() ?: modelDue,
                priority = Priority.entries.firstOrNull { it.name == answer.priority.uppercase() } ?: Priority.NORMAL,
                requiresAlarm = answer.requiresAlarm || deterministic?.hasExplicitTime == true,
                confidence = answer.confidence.coerceIn(0.0, 1.0),
                engine = engine,
                ambiguities = ambiguities,
            ),
        )
    }

    /** Ids are compared case-insensitively ("Health" from the model is "health"). */
    private fun ClassificationRequest.categoryIdOf(raw: String): String? =
        categories.firstOrNull { it.id == raw.trim() }?.id ?: categories.firstOrNull { it.id.equals(raw.trim(), ignoreCase = true) }?.id

    private companion object {
        const val CLASSIFY_PATH = "/api/classify"
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }
}
