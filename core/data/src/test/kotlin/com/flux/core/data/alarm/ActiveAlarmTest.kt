package com.flux.core.data.alarm

import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActiveAlarmTest {
    private val alarm = ActiveAlarm().apply { start(NOTE) }

    @Test
    fun `stopping the alarm counts as handled, so it is not reported as missed`() = runTest {
        val handled = async { alarm.awaitHandled(NOTE, TIMEOUT_MS) }
        runCurrent()
        alarm.stop(NOTE)
        assertTrue(handled.await())
    }

    @Test
    fun `another reminder taking over counts as handled`() = runTest {
        val handled = async { alarm.awaitHandled(NOTE, TIMEOUT_MS) }
        runCurrent()
        alarm.start("other")
        assertTrue(handled.await())
    }

    @Test
    fun `nobody acting before the time runs out is not handled`() = runTest {
        val handled = async { alarm.awaitHandled(NOTE, TIMEOUT_MS) }
        advanceTimeBy(TIMEOUT_MS + 1)
        assertFalse(handled.await())
    }

    private companion object {
        const val NOTE = "n1"
        const val TIMEOUT_MS = 60_000L
    }
}
