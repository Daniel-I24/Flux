package com.flux.mobile.di

import com.flux.core.ai.local.LocalNoteClassifier
import com.flux.core.ai.remote.RemoteClassifierConfig
import com.flux.core.ai.remote.RemoteCredentials
import com.flux.core.ai.remote.RemoteNoteClassifier
import com.flux.core.domain.port.NoteClassifier
import com.flux.core.domain.port.ReclassificationQueue
import com.flux.core.domain.port.RemoteAiStatus
import com.flux.core.domain.usecase.RemoteClassifier
import com.flux.mobile.ai.ReclassificationScheduler
import com.flux.mobile.config.ServerConfig
import com.flux.mobile.data.firebase.FirebaseRemoteCredentials
import com.flux.mobile.data.remote.ServerHealthChecker
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ClassifierModule {
    @Provides
    @Singleton
    fun okHttp(): OkHttpClient = OkHttpClient.Builder().retryOnConnectionFailure(true).build()

    @Provides
    @Singleton
    fun remoteClassifier(client: OkHttpClient, server: ServerConfig, credentials: RemoteCredentials): RemoteNoteClassifier =
        RemoteNoteClassifier(client, RemoteClassifierConfig(server.baseUrl, server.classifyTimeoutMs), credentials)

    /** Used alone to reclassify notes that were classified offline. */
    @Provides
    @RemoteClassifier
    fun remoteOnly(remote: RemoteNoteClassifier): NoteClassifier = remote

    /** Strategy order = priority: remote Gemini first, local NLP as the guaranteed fallback. */
    @Provides
    fun classifiers(remote: RemoteNoteClassifier, local: LocalNoteClassifier): List<@JvmSuppressWildcards NoteClassifier> =
        listOf(remote, local)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CredentialsModule {
    @Binds abstract fun remoteCredentials(impl: FirebaseRemoteCredentials): RemoteCredentials

    @Binds abstract fun reclassificationQueue(impl: ReclassificationScheduler): ReclassificationQueue

    @Binds abstract fun remoteAiStatus(impl: ServerHealthChecker): RemoteAiStatus
}
