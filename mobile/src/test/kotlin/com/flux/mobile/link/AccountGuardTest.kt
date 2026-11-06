package com.flux.mobile.link

import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.usecase.ObserveAccountLinkUseCase
import com.flux.core.domain.usecase.SignOutUseCase
import com.flux.core.model.AccountLink
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AccountGuardTest {
    private val link = MutableStateFlow<AccountLink?>(null)
    private val signOut = mockk<SignOutUseCase>(relaxed = true)
    private val observe = mockk<ObserveAccountLinkUseCase> { every { this@mockk.invoke() } returns link }
    private val installation = mockk<InstallationIdProvider> { coEvery { installationId() } returns MINE }
    private val guard = AccountGuard(observe, installation, signOut)

    private fun TestScope.started() = guard.start(backgroundScope)

    private fun ownedBy(id: String) = AccountLink(phoneInstallationId = id, watchNodeId = null, linkedAt = null)

    @Test
    fun `a transfer to another phone signs this one out and says why`() = runTest(UnconfinedTestDispatcher()) {
        started()
        link.value = ownedBy(MINE)
        coVerify(exactly = 0) { signOut() }
        link.value = ownedBy(OTHER)
        coVerify(exactly = 1) { signOut() }
        assertTrue(guard.signedOutElsewhere.value)
    }

    @Test
    fun `signing in to an account another phone owns keeps the session for the transfer question`() = runTest(UnconfinedTestDispatcher()) {
        started()
        guard.beginSignIn()
        link.value = ownedBy(OTHER)
        coVerify(exactly = 0) { signOut() }
        link.value = ownedBy(MINE)
        guard.endSignIn()
        coVerify(exactly = 0) { signOut() }
        assertFalse(guard.signedOutElsewhere.value)
    }

    @Test
    fun `a failed sign-in after being displaced does not sign out or warn again`() = runTest(UnconfinedTestDispatcher()) {
        started()
        link.value = ownedBy(OTHER)
        link.value = null
        guard.beginSignIn()
        assertFalse(guard.signedOutElsewhere.value)
        guard.endSignIn()
        coVerify(exactly = 1) { signOut() }
        assertFalse(guard.signedOutElsewhere.value)
    }

    private companion object {
        const val MINE = "this-phone"
        const val OTHER = "other-phone"
    }
}
