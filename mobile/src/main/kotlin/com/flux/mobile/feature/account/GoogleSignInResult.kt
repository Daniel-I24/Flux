package com.flux.mobile.feature.account

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.flux.mobile.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException

sealed interface GoogleSignInResult {
    data class Token(val idToken: String) : GoogleSignInResult
    data class Error(val message: Int) : GoogleSignInResult
}

/** Credential Manager flow; needs an Activity context, so it lives in the UI layer. */
suspend fun requestGoogleIdToken(context: Context, webClientId: String): GoogleSignInResult {
    val option = GetGoogleIdOption.Builder()
        .setServerClientId(webClientId)
        .setFilterByAuthorizedAccounts(false)
        .build()
    val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
    return try {
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        GoogleSignInResult.Token(GoogleIdTokenCredential.createFrom(credential.data).idToken)
    } catch (_: GetCredentialCancellationException) {
        GoogleSignInResult.Error(R.string.error_cancelled)
    } catch (_: GetCredentialException) {
        GoogleSignInResult.Error(R.string.error_unexpected)
    } catch (_: GoogleIdTokenParsingException) {
        GoogleSignInResult.Error(R.string.error_unexpected)
    }
}
