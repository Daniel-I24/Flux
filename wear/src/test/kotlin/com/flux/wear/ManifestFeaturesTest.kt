package com.flux.wear

import android.app.Application
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Any Wear OS watch must be able to install FLUX: only "is a watch" is required, the microphone is optional. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ManifestFeaturesTest {
    @Test
    fun `only the watch type is required and the microphone is optional`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val features = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_CONFIGURATIONS).reqFeatures.orEmpty()
        val microphone = features.single { it.name == PackageManager.FEATURE_MICROPHONE }
        assertEquals(0, microphone.flags and FeatureInfo.FLAG_REQUIRED)
        val required = features.filter { it.flags and FeatureInfo.FLAG_REQUIRED != 0 }.map { it.name }
        assertEquals(listOf(PackageManager.FEATURE_WATCH), required)
    }
}
