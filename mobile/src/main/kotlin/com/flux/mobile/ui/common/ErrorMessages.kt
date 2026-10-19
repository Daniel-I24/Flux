package com.flux.mobile.ui.common

import androidx.annotation.StringRes
import com.flux.core.model.AppError
import com.flux.core.model.AuthFailure
import com.flux.mobile.R

/** Single mapping from the error model to user-facing Spanish text. */
@StringRes
fun AppError.messageRes(): Int = when (this) {
    AppError.Network -> R.string.error_network
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.NotFound -> R.string.error_not_found
    AppError.InvalidInput -> R.string.error_invalid_input
    AppError.ServerUnavailable -> R.string.error_server
    AppError.Storage -> R.string.error_storage
    AppError.NotConfigured -> R.string.error_not_configured
    is AppError.Auth -> when (reason) {
        AuthFailure.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
        AuthFailure.EMAIL_IN_USE -> R.string.error_email_in_use
        AuthFailure.WEAK_PASSWORD -> R.string.error_weak_password
        AuthFailure.REAUTH_REQUIRED -> R.string.error_reauth_required
        AuthFailure.CANCELLED -> R.string.error_cancelled
    }
    is AppError.Unexpected -> R.string.error_unexpected
}
