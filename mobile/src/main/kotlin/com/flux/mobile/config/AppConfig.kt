package com.flux.mobile.config

import com.flux.mobile.BuildConfig

/** Values injected at build time from local.properties (see local.properties.example). */
data class FirebaseConfig(val apiKey: String, val appId: String, val projectId: String, val webClientId: String) {
    val isConfigured: Boolean get() = apiKey.isNotBlank() && appId.isNotBlank() && projectId.isNotBlank()
    val supportsGoogleSignIn: Boolean get() = isConfigured && webClientId.isNotBlank()

    companion object {
        fun fromBuildConfig() = FirebaseConfig(
            apiKey = BuildConfig.FIREBASE_API_KEY,
            appId = BuildConfig.FIREBASE_APP_ID,
            projectId = BuildConfig.FIREBASE_PROJECT_ID,
            webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
        )
    }
}

data class ServerConfig(val baseUrl: String, val classifyTimeoutMs: Long) {
    companion object {
        fun fromBuildConfig() = ServerConfig(BuildConfig.API_BASE_URL, BuildConfig.CLASSIFY_TIMEOUT_MS)
    }
}
