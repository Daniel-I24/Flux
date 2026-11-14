package com.flux.mobile

import android.app.Application
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Any Android phone must be able to install FLUX: the microphone is optional (typing works). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ManifestFeaturesTest {
    @Test
    fun `the microphone is declared optional and no other hardware is required`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val features = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_CONFIGURATIONS).reqFeatures.orEmpty()
        val microphone = features.single { it.name == PackageManager.FEATURE_MICROPHONE }
        assertEquals(0, microphone.flags and FeatureInfo.FLAG_REQUIRED)
        assertTrue(features.none { it.flags and FeatureInfo.FLAG_REQUIRED != 0 })
    }
}
