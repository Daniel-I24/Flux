package com.flux.core.data.permissions

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.provider.Settings
import androidx.core.net.toUri
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PermissionChecksTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `the phone asks to appear on top, the watch does not, and exact alarms come with the app on Android 13+`() {
        val phone = PermissionChecks(context, isWatch = false).applicable()
        val watch = PermissionChecks(context, isWatch = true).applicable()
        assertTrue(AppPermission.OVERLAY in phone)
        assertFalse(AppPermission.OVERLAY in watch)
        assertTrue(listOf(AppPermission.MICROPHONE, AppPermission.NOTIFICATIONS, AppPermission.BATTERY).all { it in watch })
        assertFalse(AppPermission.EXACT_ALARMS in phone)
        // No brand screens on a stock phone: the brand steps are hidden.
        assertFalse(AppPermission.AUTOSTART in phone)
    }

    @Test
    fun `the state is read live from the system`() {
        val checks = PermissionChecks(context, isWatch = false)
        ShadowSettings.setCanDrawOverlays(false)
        assertEquals(PermissionStatus.PENDING, checks.status(AppPermission.OVERLAY))
        ShadowSettings.setCanDrawOverlays(true)
        assertEquals(PermissionStatus.GRANTED, checks.status(AppPermission.OVERLAY))
        assertEquals(PermissionStatus.UNKNOWN, checks.status(AppPermission.AUTOSTART))
    }

    @Test
    fun `each permission opens its exact screen, or FLUX's app info when the phone lacks it`() {
        val checks = PermissionChecks(context, isWatch = false)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, checks.settingsIntent(AppPermission.BATTERY).action)
        val battery = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:${context.packageName}".toUri())
        shadowOf(context.packageManager).addResolveInfoForIntent(
            battery,
            ResolveInfo().apply {
                activityInfo = ActivityInfo().apply {
                    packageName = "android"
                    name = "Battery"
                }
            },
        )
        val intent = checks.settingsIntent(AppPermission.BATTERY)
        assertEquals(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, intent.action)
        assertEquals("package:${context.packageName}", intent.dataString)
    }

    @Test
    fun `a Wear OS placeholder screen falls back to FLUX's app info`() {
        val battery = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:${context.packageName}".toUri())
        shadowOf(context.packageManager).addResolveInfoForIntent(
            battery,
            ResolveInfo().apply {
                activityInfo = ActivityInfo().apply {
                    packageName = "com.google.android.apps.wearable.settings"
                    name = "com.google.android.clockwork.settings.FakeSettingsActivity"
                }
            },
        )
        val intent = PermissionChecks(context, isWatch = true).settingsIntent(AppPermission.BATTERY)
        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
    }
}
