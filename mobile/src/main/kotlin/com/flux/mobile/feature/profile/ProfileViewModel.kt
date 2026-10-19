package com.flux.mobile.feature.profile

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.ProfileField
import com.flux.core.domain.usecase.SaveProfileUseCase
import com.flux.core.model.FluxResult
import com.flux.core.model.Occupation
import com.flux.mobile.ui.common.messageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val age: String = "",
    val occupation: Occupation = Occupation.STUDENT,
    val invalid: Set<ProfileField> = emptySet(),
    val saving: Boolean = false,
    @StringRes val error: Int? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase,
) : ViewModel() {
    private val state = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = state.asStateFlow()

    init {
        viewModelScope.launch {
            observeProfile().first()?.let { profile ->
                state.update {
                    it.copy(name = profile.displayName, age = profile.age?.toString().orEmpty(), occupation = profile.occupation)
                }
            }
        }
    }

    fun onNameChange(value: String) = state.update { it.copy(name = value, invalid = it.invalid - ProfileField.NAME) }

    fun onAgeChange(value: String) = state.update {
        it.copy(
            age = value.filter(Char::isDigit).take(MAX_AGE_DIGITS),
            invalid =
            it.invalid - ProfileField.AGE
        )
    }

    fun onOccupationChange(value: Occupation) = state.update { it.copy(occupation = value) }

    fun save(onSaved: () -> Unit) {
        val current = state.value
        val invalid = saveProfile.validate(current.name, current.age)
        if (invalid.isNotEmpty()) {
            state.update { it.copy(invalid = invalid) }
            return
        }
        viewModelScope.launch {
            state.update { it.copy(saving = true, error = null) }
            when (val result = saveProfile(current.name, current.age, current.occupation)) {
                is FluxResult.Success -> onSaved()
                is FluxResult.Failure -> state.update { it.copy(error = result.error.messageRes()) }
            }
            state.update { it.copy(saving = false) }
        }
    }

    private companion object {
        const val MAX_AGE_DIGITS = 3
    }
}
