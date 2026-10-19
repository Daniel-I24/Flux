package com.flux.mobile.feature.account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.flux.core.designsystem.theme.FluxSizes
import com.flux.mobile.BuildConfig
import com.flux.mobile.R
import kotlinx.coroutines.launch

/** Email/password fields, mode switch and "Continuar con Google". */
@Composable
fun AccountFields(state: AccountUiState, viewModel: AccountViewModel) {
    OutlinedTextField(
        value = state.email,
        onValueChange = viewModel::onEmailChange,
        label = { Text(stringResource(R.string.account_email)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    PasswordField(state, viewModel)
    TextButton(onClick = viewModel::toggleMode, modifier = Modifier.heightIn(min = FluxSizes.minTouchTarget)) {
        Text(stringResource(if (state.createAccount) R.string.account_switch_to_sign_in else R.string.account_switch_to_sign_up))
    }
    if (state.googleAvailable) GoogleButton(viewModel)
}

@Composable
private fun PasswordField(state: AccountUiState, viewModel: AccountViewModel) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = state.password,
        onValueChange = viewModel::onPasswordChange,
        label = { Text(stringResource(R.string.account_password)) },
        supportingText = { if (state.createAccount) Text(stringResource(R.string.account_password_hint)) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = stringResource(if (visible) R.string.account_hide_password else R.string.account_show_password),
                )
            }
        },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun GoogleButton(viewModel: AccountViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    OutlinedButton(
        onClick = {
            scope.launch {
                when (val result = requestGoogleIdToken(context, BuildConfig.GOOGLE_WEB_CLIENT_ID)) {
                    is GoogleSignInResult.Token -> viewModel.onGoogleToken(result.idToken)
                    is GoogleSignInResult.Error -> viewModel.onGoogleError(result.message)
                }
            }
        },
        modifier = Modifier.fillMaxWidth().heightIn(min = FluxSizes.minTouchTarget),
    ) { Text(stringResource(R.string.account_google)) }
}
