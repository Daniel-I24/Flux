package com.flux.mobile

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.flux.mobile.data.firebase.FirebaseBootstrap
import com.flux.mobile.startup.AppStartup
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FluxMobileApp :
    Application(),
    Configuration.Provider {
    @Inject lateinit var firebase: FirebaseBootstrap

    @Inject lateinit var startup: AppStartup

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        firebase.initialize()
        startup.start()
    }
}
