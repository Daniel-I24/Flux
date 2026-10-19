package com.flux.mobile.di

import com.flux.core.domain.repository.AccountLinkRepository
import com.flux.core.domain.repository.AuthRepository
import com.flux.mobile.config.FirebaseConfig
import com.flux.mobile.config.ServerConfig
import com.flux.mobile.data.firebase.FirebaseAuthRepository
import com.flux.mobile.data.firebase.FirestoreAccountLinkRepository
import com.flux.mobile.data.local.LocalAccountLinkRepository
import com.flux.mobile.data.local.LocalAuthRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Chooses Firebase or local adapters once, based on the build configuration. */
@Module
@InstallIn(SingletonComponent::class)
object AccountModule {
    @Provides
    @Singleton
    fun firebaseConfig(): FirebaseConfig = FirebaseConfig.fromBuildConfig()

    @Provides
    @Singleton
    fun serverConfig(): ServerConfig = ServerConfig.fromBuildConfig()

    @Provides
    @Singleton
    fun authRepository(
        config: FirebaseConfig,
        firebase: Lazy<FirebaseAuthRepository>,
        local: Lazy<LocalAuthRepository>,
    ): AuthRepository = if (config.isConfigured) firebase.get() else local.get()

    @Provides
    @Singleton
    fun accountLinkRepository(
        config: FirebaseConfig,
        firestore: Lazy<FirestoreAccountLinkRepository>,
        local: Lazy<LocalAccountLinkRepository>,
    ): AccountLinkRepository = if (config.isConfigured) firestore.get() else local.get()
}
