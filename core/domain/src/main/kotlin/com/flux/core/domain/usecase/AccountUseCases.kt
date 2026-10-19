package com.flux.core.domain.usecase

import com.flux.core.domain.policy.PhoneOwnershipPolicy
import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.repository.AccountLinkRepository
import com.flux.core.domain.repository.AuthRepository
import com.flux.core.domain.repository.AuthSession
import com.flux.core.model.AppError
import com.flux.core.model.AuthFailure
import com.flux.core.model.FluxResult
import com.flux.core.model.PhoneOwnership
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

object CredentialRules {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    const val MIN_PASSWORD = 8
    fun isValidEmail(email: String) = EMAIL.matches(email.trim())
    fun isValidPassword(password: String) = password.length >= MIN_PASSWORD
}

class ObserveSessionUseCase @Inject constructor(private val auth: AuthRepository) {
    operator fun invoke(): Flow<AuthSession?> = auth.observeSession()
}

class SignInWithEmailUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, createAccount: Boolean): FluxResult<AuthSession> {
        if (!CredentialRules.isValidEmail(email)) return FluxResult.Failure(AppError.InvalidInput)
        if (createAccount && !CredentialRules.isValidPassword(password)) {
            return FluxResult.Failure(AppError.Auth(AuthFailure.WEAK_PASSWORD))
        }
        return if (createAccount) auth.signUp(email.trim(), password) else auth.signIn(email.trim(), password)
    }
}

class SignInWithGoogleUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke(idToken: String) = auth.signInWithGoogle(idToken)
}

/** Without Firebase configuration FLUX still works with a local, device-only account. */
class StartLocalSessionUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke() = auth.startLocalSession()
}

/** One phone per account: claims the account when free, reports when another phone owns it. */
class CheckPhoneOwnershipUseCase @Inject constructor(
    private val links: AccountLinkRepository,
    private val installation: InstallationIdProvider,
) {
    suspend operator fun invoke(uid: String): FluxResult<PhoneOwnership> {
        val link = when (val result = links.getLink(uid)) {
            is FluxResult.Success -> result.value
            is FluxResult.Failure -> return result
        }
        val installationId = installation.installationId()
        val ownership = PhoneOwnershipPolicy.evaluate(link, installationId)
        if (ownership == PhoneOwnership.Unclaimed) {
            val claimed = links.claimPhone(uid, installationId)
            if (claimed is FluxResult.Failure) return claimed
        }
        return FluxResult.Success(ownership)
    }
}

/** "Transferir a este celular": requires a recent login; the old phone signs out when it notices. */
class TransferAccountToThisPhoneUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val links: AccountLinkRepository,
    private val installation: InstallationIdProvider,
) {
    suspend operator fun invoke(password: String?): FluxResult<Unit> {
        val session = auth.currentSession() ?: return FluxResult.Failure(AppError.Unauthorized)
        if (password != null) {
            val reauth = auth.reauthenticate(password)
            if (reauth is FluxResult.Failure) return reauth
        }
        return links.claimPhone(session.uid, installation.installationId())
    }
}

class SignOutUseCase @Inject constructor(private val auth: AuthRepository) {
    suspend operator fun invoke() = auth.signOut()
}
