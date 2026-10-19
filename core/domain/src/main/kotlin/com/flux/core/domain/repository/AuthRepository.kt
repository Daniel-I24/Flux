package com.flux.core.domain.repository

import com.flux.core.model.AccountLink
import com.flux.core.model.FluxResult
import kotlinx.coroutines.flow.Flow

data class AuthSession(val uid: String, val email: String?, val displayName: String?, val isLocal: Boolean)

/** Adapter over Firebase Auth (or a local account when Firebase is not configured). */
interface AuthRepository {
    val isRemoteConfigured: Boolean
    fun observeSession(): Flow<AuthSession?>
    suspend fun currentSession(): AuthSession?
    suspend fun signIn(email: String, password: String): FluxResult<AuthSession>
    suspend fun signUp(email: String, password: String): FluxResult<AuthSession>
    suspend fun signInWithGoogle(idToken: String): FluxResult<AuthSession>
    suspend fun startLocalSession(): FluxResult<AuthSession>
    suspend fun reauthenticate(password: String): FluxResult<Unit>
    suspend fun idToken(forceRefresh: Boolean = false): String?
    suspend fun signOut()
}

/** users/{uid}.link in Firestore: one phone and one watch per account. */
interface AccountLinkRepository {
    fun observeLink(uid: String): Flow<AccountLink?>
    suspend fun getLink(uid: String): FluxResult<AccountLink?>
    suspend fun claimPhone(uid: String, installationId: String): FluxResult<Unit>
    suspend fun linkWatch(uid: String, watchNodeId: String): FluxResult<Unit>
    suspend fun unlinkWatch(uid: String): FluxResult<Unit>
    suspend fun saveProfileSnapshot(uid: String, displayName: String): FluxResult<Unit>
}
