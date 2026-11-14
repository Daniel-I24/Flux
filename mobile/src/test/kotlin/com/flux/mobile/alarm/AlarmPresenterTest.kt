package com.flux.mobile.alarm

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AlarmPresenterTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `with appear on top granted the alarm screen opens over the app in use`() {
        ShadowSettings.setCanDrawOverlays(true)
        AlarmPresenter(context).present("note-1")
        val started = shadowOf(context).nextStartedActivity
        assertEquals(AlarmActivity::class.java.name, started.component?.className)
        assertEquals("note-1", started.getStringExtra(PhoneAlarmTarget.EXTRA_NOTE_ID))
    }

    @Test
    fun `without it only the insistent notification is shown`() {
        ShadowSettings.setCanDrawOverlays(false)
        AlarmPresenter(context).present("note-1")
        assertNull(shadowOf(context).nextStartedActivity)
    }
}
