package com.flux.mobile.data.firebase

import com.flux.core.domain.repository.AuthRepository
import com.flux.core.domain.repository.AuthSession
import com.flux.core.model.AppError
import com.flux.core.model.FluxResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await

/** Adapter over Firebase Authentication (email/password and Google). */
class FirebaseAuthRepository @Inject constructor() : AuthRepository {
    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    override val isRemoteConfigured = true

    override fun observeSession(): Flow<AuthSession?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toSession()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override suspend fun currentSession(): AuthSession? = auth.currentUser?.toSession()

    override suspend fun signIn(email: String, password: String) =
        firebaseCall { requireNotNull(auth.signInWithEmailAndPassword(email, password).await().user).toSession() }

    override suspend fun signUp(email: String, password: String) =
        firebaseCall { requireNotNull(auth.createUserWithEmailAndPassword(email, password).await().user).toSession() }

    override suspend fun signInWithGoogle(idToken: String) = firebaseCall {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        requireNotNull(auth.signInWithCredential(credential).await().user).toSession()
    }

    override suspend fun startLocalSession(): FluxResult<AuthSession> = FluxResult.Failure(AppError.NotConfigured)

    override suspend fun reauthenticate(password: String): FluxResult<Unit> {
        val user = auth.currentUser ?: return FluxResult.Failure(AppError.Unauthorized)
        val email = user.email ?: return FluxResult.Failure(AppError.Unauthorized)
        return firebaseCall { user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await() }
            .let { if (it is FluxResult.Failure) it else FluxResult.Success(Unit) }
    }

    override suspend fun idToken(forceRefresh: Boolean): String? =
        runCatching { auth.currentUser?.getIdToken(forceRefresh)?.await()?.token }.getOrNull()

    override suspend fun signOut() = auth.signOut()

    private fun FirebaseUser.toSession() = AuthSession(uid = uid, email = email, displayName = displayName, isLocal = false)
}
