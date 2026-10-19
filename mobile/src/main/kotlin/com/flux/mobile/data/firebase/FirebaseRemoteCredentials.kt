package com.flux.mobile.data.firebase

import com.flux.core.ai.remote.RemoteCredentials
import com.flux.core.domain.repository.AuthRepository
import com.flux.mobile.config.FirebaseConfig
import com.google.firebase.appcheck.FirebaseAppCheck
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

/** Firebase ID token + App Check token attached to every /api/classify call. */
class FirebaseRemoteCredentials @Inject constructor(
    private val auth: AuthRepository,
    private val config: FirebaseConfig,
) : RemoteCredentials {
    override suspend fun idToken(): String? = auth.idToken()

    override suspend fun appCheckToken(): String? {
        if (!config.isConfigured) return null
        return runCatching { FirebaseAppCheck.getInstance().getAppCheckToken(false).await().token }.getOrNull()
    }
}
