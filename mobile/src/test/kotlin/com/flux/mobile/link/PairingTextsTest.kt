package com.flux.mobile.link

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.flux.mobile.R
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Pairing help must work for any brand: the watch's own app, with Galaxy Wearable and Pixel Watch only as examples. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class PairingTextsTest {
    @Test
    fun `pairing help names the watch app generically`() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        listOf(R.string.link_no_watch, R.string.link_chooser_empty).forEach { id ->
            assertTrue(context.getString(id).contains(GENERIC))
        }
    }

    private companion object {
        const val GENERIC = "la app de tu reloj (Galaxy Wearable, Pixel Watch u otra)"
    }
}
