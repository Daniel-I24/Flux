package com.flux.mobile.sync

import com.flux.core.domain.repository.OnboardingStep
import com.flux.core.domain.usecase.CurrentAccountIdUseCase
import com.flux.core.domain.usecase.ObserveOnboardingStepUseCase
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.SyncMessage
import com.flux.core.sync.protocol.WatchOwnership
import com.flux.mobile.data.remote.ServerHealthChecker
import javax.inject.Inject
import kotlinx.coroutines.flow.first

/** Builds the answer to a watch's Hello: account readiness and who owns the watch. */
class PhoneStatusProvider @Inject constructor(
    private val currentAccountId: CurrentAccountIdUseCase,
    private val observeStep: ObserveOnboardingStepUseCase,
    private val observeProfile: ObserveProfileUseCase,
    private val serverHealth: ServerHealthChecker,
) {
    suspend fun statusFor(watchLinkedAccountId: String?): SyncMessage.Status {
        val accountId = currentAccountId()
        val accountState = when {
            accountId == null -> AccountState.SIGNED_OUT
            observeStep().first() != OnboardingStep.DONE -> AccountState.ONBOARDING
            else -> AccountState.READY
        }
        val ownership = when (watchLinkedAccountId) {
            null -> WatchOwnership.NOT_LINKED
            accountId -> WatchOwnership.LINKED_HERE
            else -> WatchOwnership.LINKED_TO_OTHER
        }
        return SyncMessage.Status(
            accountState = accountState,
            ownership = ownership,
            userName = observeProfile().first()?.displayName,
            serverReachable = accountState == AccountState.READY && serverHealth.isReachable(),
        )
    }
}
