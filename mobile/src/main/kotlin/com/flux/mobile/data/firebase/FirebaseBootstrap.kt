package com.flux.mobile.data.firebase

import android.content.Context
import com.flux.mobile.config.FirebaseConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Initializes Firebase from build-time options instead of google-services.json,
 * so the project builds without credentials. App Check protects calls to our server.
 */
@Singleton
class FirebaseBootstrap @Inject constructor(
    @ApplicationContext private val context: Context,
    private val config: FirebaseConfig,
) {
    fun initialize() {
        if (!config.isConfigured || FirebaseApp.getApps(context).isNotEmpty()) return
        val options = FirebaseOptions.Builder()
            .setApiKey(config.apiKey)
            .setApplicationId(config.appId)
            .setProjectId(config.projectId)
            .build()
        FirebaseApp.initializeApp(context, options)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(appCheckProviderFactory())
    }
}
