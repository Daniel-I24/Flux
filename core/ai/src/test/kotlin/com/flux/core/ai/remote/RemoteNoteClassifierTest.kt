package com.flux.core.ai.remote

import com.flux.core.domain.port.ClassificationRequest
import com.flux.core.model.AppError
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.FluxResult
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RemoteNoteClassifierTest {
    private val server = MockWebServer()
    private val zone = ZoneId.of("America/Bogota")
    private val now = ZonedDateTime.of(2026, 10, 18, 10, 0, 0, 0, zone)
    private val request = ClassificationRequest(
        "Recuérdame tomar agua en 10 segundos",
        now.toInstant().toEpochMilli(),
        zone,
        DefaultCategory.all(),
        null,
    )

    private class FakeCredentials(private val token: String?) : RemoteCredentials {
        override suspend fun idToken() = token
        override suspend fun appCheckToken() = "app-check"
    }

    @Before fun start() = server.start()

    @After fun stop() = server.shutdown()

    private fun classifier(token: String? = "id-token", timeoutMs: Long = 2_000) = RemoteNoteClassifier(
        OkHttpClient(),
        RemoteClassifierConfig(server.url("/").toString(), timeoutMs),
        FakeCredentials(token),
    )

    @Test
    fun `sends credentials and parses the answer, with the deterministic date winning`() = runTest {
        val answer = """{"categoryId":"reminders","title":"Tomar agua","dueAt":"2030-01-01T00:00:00Z",""" +
            """"priority":"HIGH","requiresAlarm":true,"confidence":0.9}"""
        server.enqueue(MockResponse().setBody(answer))
        val result = classifier().classify(request) as FluxResult.Success
        val recorded = server.takeRequest()
        assertEquals("/api/classify", recorded.path)
        assertEquals("Bearer id-token", recorded.getHeader("Authorization"))
        assertEquals("app-check", recorded.getHeader("X-Firebase-AppCheck"))
        assertEquals(ClassifierEngine.REMOTE, result.value.engine)
        assertEquals(now.plusSeconds(10).toInstant().toEpochMilli(), result.value.dueAt)
        assertTrue(result.value.requiresAlarm)
    }

    @Test
    fun `fails without a session so the chain uses the local classifier`() = runTest {
        assertEquals(FluxResult.Failure(AppError.Unauthorized), classifier(token = null).classify(request))
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `maps server errors and rejected tokens`() = runTest {
        server.enqueue(MockResponse().setResponseCode(503))
        assertEquals(FluxResult.Failure(AppError.ServerUnavailable), classifier().classify(request))
        server.enqueue(MockResponse().setResponseCode(401))
        assertEquals(FluxResult.Failure(AppError.Unauthorized), classifier().classify(request))
    }

    @Test
    fun `rejects unknown categories and malformed answers`() = runTest {
        server.enqueue(MockResponse().setBody("""{"categoryId":"nope","title":"x"}"""))
        assertEquals(FluxResult.Failure(AppError.InvalidInput), classifier().classify(request))
        server.enqueue(MockResponse().setBody("not json"))
        assertEquals(FluxResult.Failure(AppError.InvalidInput), classifier().classify(request))
    }

    @Test
    fun `times out when the server is slow`() = runTest {
        server.enqueue(MockResponse().setBody("{}").setBodyDelay(2, TimeUnit.SECONDS))
        assertEquals(FluxResult.Failure(AppError.Timeout), classifier(timeoutMs = 300).classify(request))
    }
}
