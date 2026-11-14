package com.flux.wear

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** "Not paired" must help on any brand: the watch's own app, with Galaxy Wearable and Pixel Watch only as examples. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class PairingTextsTest {
    @Test
    fun `the not paired message names the watch app generically`() {
        val text = ApplicationProvider.getApplicationContext<Application>().getString(R.string.problem_no_phone)
        assertTrue(text.contains("la app de tu reloj (Galaxy Wearable, Pixel Watch u otra)"))
    }
}
