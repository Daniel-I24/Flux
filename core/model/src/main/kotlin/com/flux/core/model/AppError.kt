package com.flux.core.model

/** Every failure the UI may need to explain; mapped to strings.xml by each app. */
sealed interface AppError {
    data object Network : AppError
    data object Timeout : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data object InvalidInput : AppError
    data object ServerUnavailable : AppError
    data object Storage : AppError
    data object NotConfigured : AppError
    data class Auth(val reason: AuthFailure) : AppError
    data class Unexpected(val message: String?) : AppError
}

enum class AuthFailure { INVALID_CREDENTIALS, EMAIL_IN_USE, WEAK_PASSWORD, REAUTH_REQUIRED, CANCELLED }

/** Lightweight result type used across layers (no exceptions leak to the UI). */
sealed interface FluxResult<out T> {
    data class Success<T>(val value: T) : FluxResult<T>
    data class Failure(val error: AppError) : FluxResult<Nothing>
}

inline fun <T, R> FluxResult<T>.map(transform: (T) -> R): FluxResult<R> = when (this) {
    is FluxResult.Success -> FluxResult.Success(transform(value))
    is FluxResult.Failure -> this
}

fun <T> FluxResult<T>.getOrNull(): T? = (this as? FluxResult.Success)?.value
