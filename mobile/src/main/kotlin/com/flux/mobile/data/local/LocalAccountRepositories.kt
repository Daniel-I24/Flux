package com.flux.mobile.data.local

import com.flux.core.data.prefs.PreferencesStore
import com.flux.core.domain.port.Clock
import com.flux.core.domain.port.InstallationIdProvider
import com.flux.core.domain.repository.AccountLinkRepository
import com.flux.core.domain.repository.AuthRepository
import com.flux.core.domain.repository.AuthSession
import com.flux.core.model.AccountLink
import com.flux.core.model.AppError
import com.flux.core.model.FluxResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
private data class StoredLink(val phoneInstallationId: String?, val watchNodeId: String?, val linkedAt: Long?)

/**
 * Used when Firebase is not configured: a single device-only account so FLUX can be
 * demonstrated without credentials. Email sign-in is unavailable in this mode.
 */
class LocalAuthRepository @Inject constructor(
    private val store: PreferencesStore,
    private val installation: InstallationIdProvider,
) : AuthRepository {
    override val isRemoteConfigured = false

    override fun observeSession(): Flow<AuthSession?> = store.observe(SESSION_KEY, String.serializer()).map { it?.let(::session) }

    override suspend fun currentSession(): AuthSession? = store.get(SESSION_KEY, String.serializer())?.let(::session)

    override suspend fun signIn(email: String, password: String): FluxResult<AuthSession> = FluxResult.Failure(AppError.NotConfigured)

    override suspend fun signUp(email: String, password: String): FluxResult<AuthSession> = FluxResult.Failure(AppError.NotConfigured)

    override suspend fun signInWithGoogle(idToken: String): FluxResult<AuthSession> = FluxResult.Failure(AppError.NotConfigured)

    override suspend fun startLocalSession(): FluxResult<AuthSession> {
        val uid = "local-${installation.installationId()}"
        store.put(SESSION_KEY, String.serializer(), uid)
        return FluxResult.Success(session(uid))
    }

    override suspend fun reauthenticate(password: String): FluxResult<Unit> = FluxResult.Success(Unit)

    override suspend fun idToken(forceRefresh: Boolean): String? = null

    override suspend fun signOut() = store.remove(SESSION_KEY)

    private fun session(uid: String) = AuthSession(uid = uid, email = null, displayName = null, isLocal = true)

    private companion object {
        const val SESSION_KEY = "local_session"
    }
}

class LocalAccountLinkRepository @Inject constructor(
    private val store: PreferencesStore,
    private val clock: Clock,
) : AccountLinkRepository {
    override fun observeLink(uid: String): Flow<AccountLink?> = store.observe(LINK_KEY, StoredLink.serializer()).map { it?.toModel() }

    override suspend fun getLink(uid: String): FluxResult<AccountLink?> =
        FluxResult.Success(store.get(LINK_KEY, StoredLink.serializer())?.toModel())

    override suspend fun claimPhone(uid: String, installationId: String) = update {
        it.copy(phoneInstallationId = installationId, linkedAt = clock.now())
    }

    override suspend fun linkWatch(uid: String, watchNodeId: String) = update { it.copy(watchNodeId = watchNodeId) }

    override suspend fun unlinkWatch(uid: String) = update { it.copy(watchNodeId = null) }

    override suspend fun saveProfileSnapshot(uid: String, displayName: String): FluxResult<Unit> = FluxResult.Success(Unit)

    private suspend fun update(change: (StoredLink) -> StoredLink): FluxResult<Unit> {
        val current = store.get(LINK_KEY, StoredLink.serializer()) ?: StoredLink(null, null, null)
        store.put(LINK_KEY, StoredLink.serializer(), change(current))
        return FluxResult.Success(Unit)
    }

    private fun StoredLink.toModel() = AccountLink(phoneInstallationId, watchNodeId, linkedAt)

    private companion object {
        const val LINK_KEY = "local_account_link"
    }
}
