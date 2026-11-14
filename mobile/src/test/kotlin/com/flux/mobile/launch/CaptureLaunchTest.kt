package com.flux.mobile.launch

import android.app.Application
import android.content.Intent
import android.service.quicksettings.TileService
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "es")
class CaptureLaunchTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `long-pressing the icon offers Dictar nota and Nueva nota`() {
        AppShortcuts(context).publish()
        val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(context).sortedBy { it.rank }
        assertEquals(listOf("Dictar nota", "Nueva nota"), shortcuts.map { it.shortLabel.toString() })
        assertEquals(CaptureLaunchRequests.ACTION_DICTATE, shortcuts.first().intent.action)
    }

    @Test
    fun `shortcut actions become a pending capture until the notes tab takes it`() {
        val requests = CaptureLaunchRequests()
        requests.handle(Intent(CaptureLaunchRequests.ACTION_DICTATE))
        assertEquals(CaptureLaunch.DICTATE, requests.request.value)
        requests.consume()
        requests.handle(Intent(Intent.ACTION_MAIN))
        assertNull(requests.request.value)
        requests.handle(Intent(CaptureLaunchRequests.ACTION_NEW_NOTE))
        assertEquals(CaptureLaunch.TYPE, requests.request.value)
    }

    @Test
    fun `the quick settings button is declared`() {
        val services = context.packageManager.queryIntentServices(Intent(TileService.ACTION_QS_TILE), 0)
        assertTrue(services.any { it.serviceInfo.name == DictateTileService::class.java.name })
    }
}
