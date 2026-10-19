package com.flux.mobile.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.core.domain.usecase.ProfileField
import com.flux.core.model.Occupation
import com.flux.mobile.R

fun Occupation.labelRes(): Int = when (this) {
    Occupation.STUDENT -> R.string.occupation_student
    Occupation.WORKER -> R.string.occupation_worker
    Occupation.BOTH -> R.string.occupation_both
    Occupation.OTHER -> R.string.occupation_other
}

/** Reused by onboarding and by Ajustes > Perfil. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileForm(state: ProfileUiState, viewModel: ProfileViewModel, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FluxSpacing.md)) {
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::onNameChange,
            label = { Text(stringResource(R.string.profile_name)) },
            isError = ProfileField.NAME in state.invalid,
            supportingText = { if (ProfileField.NAME in state.invalid) Text(stringResource(R.string.profile_name_error)) },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.age,
            onValueChange = viewModel::onAgeChange,
            label = { Text(stringResource(R.string.profile_age)) },
            isError = ProfileField.AGE in state.invalid,
            supportingText = { if (ProfileField.AGE in state.invalid) Text(stringResource(R.string.profile_age_error)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.profile_occupation), style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(FluxSpacing.sm)) {
            Occupation.entries.forEach { occupation ->
                FilterChip(
                    selected = state.occupation == occupation,
                    onClick = { viewModel.onOccupationChange(occupation) },
                    label = { Text(stringResource(occupation.labelRes())) },
                )
            }
        }
        state.error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
    }
}
