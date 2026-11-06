package com.flux.mobile.link

import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.usecase.ObserveAccountLinkUseCase
import com.flux.core.domain.usecase.SignOutUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * One phone per account: when the account is transferred to another phone, Firestore
 * notifies this one (snapshot listener, no polling) and it signs out.
 */
@Singleton
class AccountGuard @Inject constructor(
    private val observeLink: ObserveAccountLinkUseCase,
    private val installation: InstallationIdProvider,
    private val signOut: SignOutUseCase,
) {
    private val signingIn = MutableStateFlow(false)
    private val displaced = MutableStateFlow(false)

    /** This phone lost the account to another one; the sign-in screen explains why. */
    val signedOutElsewhere: StateFlow<Boolean> = displaced.asStateFlow()

    /**
     * A sign-in in progress may offer "Transferir a este celular" for an account another phone still
     * owns: that session must survive until the user decides.
     */
    fun beginSignIn() {
        displaced.value = false
        signingIn.value = true
    }

    fun endSignIn() {
        signingIn.value = false
    }

    /** The user declined the transfer: the session goes away before the guard resumes. */
    suspend fun cancelSignIn() {
        signOut()
        endSignIn()
    }

    fun start(scope: CoroutineScope) {
        scope.launch {
            val mine = installation.installationId()
            combine(observeLink(), signingIn) { link, signingIn ->
                val owner = link?.phoneInstallationId
                !signingIn && owner != null && owner != mine
            }.filter { it }.collect {
                displaced.value = true
                signOut()
            }
        }
    }
}
