package com.flux.mobile.data.firebase

import com.flux.core.model.AppError
import com.flux.core.model.AuthFailure
import com.flux.core.model.FluxResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlin.coroutines.cancellation.CancellationException

/** Translates Firebase exceptions into the app's error model (Adapter). */
internal fun Throwable.toAppError(): AppError = when (this) {
    is FirebaseAuthWeakPasswordException -> AppError.Auth(AuthFailure.WEAK_PASSWORD)
    is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> AppError.Auth(AuthFailure.INVALID_CREDENTIALS)
    is FirebaseAuthUserCollisionException -> AppError.Auth(AuthFailure.EMAIL_IN_USE)
    is FirebaseAuthRecentLoginRequiredException -> AppError.Auth(AuthFailure.REAUTH_REQUIRED)
    is FirebaseNetworkException -> AppError.Network
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED, FirebaseFirestoreException.Code.UNAUTHENTICATED -> AppError.Unauthorized
        FirebaseFirestoreException.Code.UNAVAILABLE, FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> AppError.Network
        else -> AppError.Unexpected(message)
    }
    else -> AppError.Unexpected(message)
}

/** Runs a Firebase call and converts failures (but never swallows cancellation). */
internal suspend fun <T> firebaseCall(block: suspend () -> T): FluxResult<T> = try {
    FluxResult.Success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
    FluxResult.Failure(error.toAppError())
}
