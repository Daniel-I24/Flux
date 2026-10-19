package com.flux.core.domain.usecase

import com.flux.core.domain.FakeProfileRepository
import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.repository.AccountLinkRepository
import com.flux.core.domain.repository.AuthRepository
import com.flux.core.domain.repository.AuthSession
import com.flux.core.model.AccountLink
import com.flux.core.model.AppError
import com.flux.core.model.AuthFailure
import com.flux.core.model.DailyRoutine
import com.flux.core.model.FluxResult
import com.flux.core.model.Occupation
import com.flux.core.model.PhoneOwnership
import com.flux.core.model.TimeOfDay
import com.flux.core.model.TimeRange
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.DayOfWeek
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountAndProfileTest {
    private val session = AuthSession("uid", "ana@flux.app", null, isLocal = false)
    private val auth = mockk<AuthRepository>(relaxed = true)
    private val links = mockk<AccountLinkRepository>(relaxed = true)
    private val installation = InstallationIdProvider { "this-phone" }

    @Test
    fun `sign in validates email and password strength before calling Firebase`() = runTest {
        val signIn = SignInWithEmailUseCase(auth)
        assertEquals(FluxResult.Failure(AppError.InvalidInput), signIn("no-es-correo", "12345678", createAccount = false))
        assertEquals(FluxResult.Failure(AppError.Auth(AuthFailure.WEAK_PASSWORD)), signIn("ana@flux.app", "123", createAccount = true))
        coVerify(exactly = 0) { auth.signIn(any(), any()) }
    }

    @Test
    fun `an unclaimed account is claimed by this phone`() = runTest {
        coEvery { links.getLink("uid") } returns FluxResult.Success(null)
        coEvery { links.claimPhone("uid", "this-phone") } returns FluxResult.Success(Unit)
        assertEquals(FluxResult.Success(PhoneOwnership.Unclaimed), CheckPhoneOwnershipUseCase(links, installation)("uid"))
        coVerify { links.claimPhone("uid", "this-phone") }
    }

    @Test
    fun `an account owned by another phone is reported and not claimed`() = runTest {
        coEvery { links.getLink("uid") } returns FluxResult.Success(AccountLink("other-phone", null, 1))
        assertEquals(FluxResult.Success(PhoneOwnership.OtherPhone), CheckPhoneOwnershipUseCase(links, installation)("uid"))
        coVerify(exactly = 0) { links.claimPhone(any(), any()) }
    }

    @Test
    fun `transfer requires reauthentication`() = runTest {
        coEvery { auth.currentSession() } returns session
        coEvery { auth.reauthenticate("bad") } returns FluxResult.Failure(AppError.Auth(AuthFailure.INVALID_CREDENTIALS))
        coEvery { auth.reauthenticate("good") } returns FluxResult.Success(Unit)
        coEvery { links.claimPhone("uid", "this-phone") } returns FluxResult.Success(Unit)
        val transfer = TransferAccountToThisPhoneUseCase(auth, links, installation)
        assertEquals(FluxResult.Failure(AppError.Auth(AuthFailure.INVALID_CREDENTIALS)), transfer("bad"))
        coVerify(exactly = 0) { links.claimPhone(any(), any()) }
        assertEquals(FluxResult.Success(Unit), transfer("good"))
    }

    @Test
    fun `profile validation`() = runTest {
        val save = SaveProfileUseCase(FakeProfileRepository())
        assertEquals(setOf(ProfileField.NAME), save.validate("  ", ""))
        assertEquals(setOf(ProfileField.AGE), save.validate("Ana", "200"))
        assertTrue(save.validate("Ana", "").isEmpty())
        val saved = save("  Ana ", "21", Occupation.STUDENT) as FluxResult.Success
        assertEquals("Ana", saved.value.displayName)
        assertEquals(21, saved.value.age)
    }

    @Test
    fun `routine requires a profile and coherent blocks`() = runTest {
        val profiles = FakeProfileRepository()
        val saveRoutine = SaveRoutineUseCase(profiles)
        assertEquals(FluxResult.Failure(AppError.NotFound), saveRoutine(DailyRoutine()))
        SaveProfileUseCase(profiles)("Ana", "", Occupation.WORKER)
        val inverted = DailyRoutine(activityBlocks = mapOf(DayOfWeek.MONDAY to TimeRange(TimeOfDay.of(18), TimeOfDay.of(8))))
        assertEquals(FluxResult.Failure(AppError.InvalidInput), saveRoutine(inverted))
        assertEquals(FluxResult.Success(Unit), saveRoutine(DailyRoutine(lunch = TimeOfDay.of(13))))
        assertEquals(TimeOfDay.of(13), profiles.profile?.routine?.lunch)
    }
}
