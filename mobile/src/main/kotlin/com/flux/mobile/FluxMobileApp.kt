package com.flux.mobile

import android.app.Application
import com.flux.core.domain.repository.CategoryRepository
import com.flux.mobile.data.firebase.FirebaseBootstrap
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class FluxMobileApp : Application() {
    @Inject lateinit var firebase: FirebaseBootstrap

    @Inject lateinit var categories: CategoryRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        firebase.initialize()
        appScope.launch { categories.ensureDefaults() }
    }
}
