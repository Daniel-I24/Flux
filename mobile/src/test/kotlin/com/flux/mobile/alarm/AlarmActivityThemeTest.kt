package com.flux.mobile.alarm

import android.app.Application
import android.content.ComponentName
import androidx.test.core.app.ApplicationProvider
import com.flux.mobile.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The full-screen alarm must not inherit the splash theme, which adds a "FLUX" title bar. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AlarmActivityThemeTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `the alarm screen declares the app theme without an action bar`() {
        val info = context.packageManager.getActivityInfo(ComponentName(context, AlarmActivity::class.java), 0)
        assertEquals(R.style.Theme_Flux, info.theme)

        val theme = context.resources.newTheme().apply { applyStyle(info.theme, true) }
        val attributes = theme.obtainStyledAttributes(intArrayOf(android.R.attr.windowActionBar))
        try {
            assertFalse(attributes.getBoolean(0, true))
        } finally {
            attributes.recycle()
        }
    }
}
