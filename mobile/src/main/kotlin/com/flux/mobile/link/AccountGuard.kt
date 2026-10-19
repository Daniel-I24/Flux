package com.flux.mobile.link

import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.usecase.ObserveAccountLinkUseCase
import com.flux.core.domain.usecase.SignOutUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNotNull
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
    fun start(scope: CoroutineScope) {
        scope.launch {
            val myInstallation = installation.installationId()
            observeLink().filterNotNull().collect { link ->
                val owner = link.phoneInstallationId
                if (owner != null && owner != myInstallation) signOut()
            }
        }
    }
}
