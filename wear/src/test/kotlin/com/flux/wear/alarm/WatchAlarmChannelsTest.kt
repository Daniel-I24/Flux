package com.flux.wear.alarm

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class WatchAlarmChannelsTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val manager = context.getSystemService(NotificationManager::class.java)

    @Test
    fun `the alarm channel vibrates on its own and replaces the silent v1_1 channel`() {
        manager.createNotificationChannel(NotificationChannel("flux_watch_alert", "old", NotificationManager.IMPORTANCE_HIGH))

        WatchAlarmChannels(context).ensure()

        val alarm = manager.getNotificationChannel(WatchAlarmChannels.ALARM)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, alarm.importance)
        assertTrue(alarm.shouldVibrate())
        assertEquals(Notification.VISIBILITY_PUBLIC, alarm.lockscreenVisibility)
        assertNull(manager.getNotificationChannel("flux_watch_alert"))
        assertFalse(WatchAlarmChannels(context).isAlarmChannelBlocked())
    }

    @Test
    fun `an alarm channel lowered by the user is reported as blocked`() {
        manager.createNotificationChannel(NotificationChannel(WatchAlarmChannels.ALARM, "lowered", NotificationManager.IMPORTANCE_LOW))
        val channels = WatchAlarmChannels(context)
        channels.ensure()
        assertTrue(channels.isAlarmChannelBlocked())
    }
}
