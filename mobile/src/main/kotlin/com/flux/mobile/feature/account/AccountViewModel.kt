package com.flux.mobile.feature.account

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.repository.AuthSession
import com.flux.core.domain.repository.OnboardingStep
import com.flux.core.domain.usecase.CheckPhoneOwnershipUseCase
import com.flux.core.domain.usecase.ObserveOnboardingStepUseCase
import com.flux.core.domain.usecase.SetOnboardingStepUseCase
import com.flux.core.domain.usecase.SignInWithEmailUseCase
import com.flux.core.domain.usecase.SignInWithGoogleUseCase
import com.flux.core.domain.usecase.SignOutUseCase
import com.flux.core.domain.usecase.StartLocalSessionUseCase
import com.flux.core.domain.usecase.TransferAccountToThisPhoneUseCase
import com.flux.core.model.FluxResult
import com.flux.core.model.PhoneOwnership
import com.flux.mobile.config.FirebaseConfig
import com.flux.mobile.ui.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AccountUiState(
    val email: String = "",
    val password: String = "",
    val createAccount: Boolean = false,
    val loading: Boolean = false,
    @StringRes val error: Int? = null,
    val askTransfer: Boolean = false,
    /** Email accounts confirm the password to transfer; Google sign-in is already a fresh login. */
    val transferNeedsPassword: Boolean = false,
    val firebaseConfigured: Boolean = true,
    val googleAvailable: Boolean = false,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    config: FirebaseConfig,
    private val signInWithEmail: SignInWithEmailUseCase,
    private val signInWithGoogle: SignInWithGoogleUseCase,
    private val startLocalSession: StartLocalSessionUseCase,
    private val checkOwnership: CheckPhoneOwnershipUseCase,
    private val transfer: TransferAccountToThisPhoneUseCase,
    private val signOut: SignOutUseCase,
    private val observeStep: ObserveOnboardingStepUseCase,
    private val setStep: SetOnboardingStepUseCase,
) : ViewModel() {
    private val state =
        MutableStateFlow(AccountUiState(firebaseConfigured = config.isConfigured, googleAvailable = config.supportsGoogleSignIn))
    val uiState: StateFlow<AccountUiState> = state.asStateFlow()

    fun onEmailChange(value: String) = state.update { it.copy(email = value, error = null) }

    fun onPasswordChange(value: String) = state.update { it.copy(password = value, error = null) }

    fun toggleMode() = state.update { it.copy(createAccount = !it.createAccount, error = null) }

    fun submit() = authenticate { signInWithEmail(state.value.email, state.value.password, state.value.createAccount) }

    fun onGoogleToken(idToken: String) = authenticate { signInWithGoogle(idToken) }

    fun onGoogleError(@StringRes message: Int) = state.update { it.copy(error = message) }

    fun continueLocally() = authenticate { startLocalSession() }

    fun confirmTransfer(password: String) = launchLoading {
        when (val result = transfer(password.takeIf { it.isNotEmpty() })) {
            is FluxResult.Success -> finish()
            is FluxResult.Failure -> state.update { it.copy(error = result.error.messageRes()) }
        }
    }

    fun cancelTransfer() = launchLoading {
        signOut()
        state.update { it.copy(askTransfer = false) }
    }

    private fun authenticate(action: suspend () -> FluxResult<AuthSession>) = launchLoading {
        when (val result = action()) {
            is FluxResult.Failure -> state.update { it.copy(error = result.error.messageRes()) }
            is FluxResult.Success -> verifyOwnership(result.value)
        }
    }

    private suspend fun verifyOwnership(session: AuthSession) {
        when (val ownership = checkOwnership(session.uid)) {
            is FluxResult.Failure -> state.update { it.copy(error = ownership.error.messageRes()) }
            is FluxResult.Success -> if (ownership.value == PhoneOwnership.OtherPhone) {
                state.update { it.copy(askTransfer = true, transferNeedsPassword = !session.isLocal && it.password.isNotEmpty()) }
            } else {
                finish()
            }
        }
    }

    private suspend fun finish() {
        state.update { it.copy(askTransfer = false) }
        if (observeStep().first() < OnboardingStep.PROFILE) setStep(OnboardingStep.PROFILE)
    }

    private fun launchLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            state.update { it.copy(loading = true, error = null) }
            block()
            state.update { it.copy(loading = false) }
        }
    }
}
