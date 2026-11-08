package com.flux.core.domain.usecase

import com.flux.core.domain.port.RemoteAiState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckRemoteAiTest {
    @Test
    fun `a sleeping server is reported as waking and then checked patiently`() = runTest {
        val asked = mutableListOf<Boolean>()
        var waking = false
        val state = CheckRemoteAiUseCase { patient ->
            asked += patient
            if (patient) RemoteAiState.AVAILABLE else RemoteAiState.WAKING
        }(onWaking = { waking = true })
        assertEquals(RemoteAiState.AVAILABLE, state)
        assertEquals(listOf(false, true), asked)
        assertTrue(waking)
    }

    @Test
    fun `no session is its own answer and is not retried`() = runTest {
        val asked = mutableListOf<Boolean>()
        var waking = false
        val state = CheckRemoteAiUseCase { patient ->
            asked += patient
            RemoteAiState.NO_SESSION
        }(onWaking = { waking = true })
        assertEquals(RemoteAiState.NO_SESSION, state)
        assertEquals(listOf(false), asked)
        assertFalse(waking)
    }
}
