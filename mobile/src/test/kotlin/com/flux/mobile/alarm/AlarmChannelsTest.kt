package com.flux.mobile.alarm

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class AlarmChannelsTest {
    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val manager = context.getSystemService(NotificationManager::class.java)

    @Test
    fun `a lowered v1_1 ringing channel is replaced by a new high-importance one`() {
        manager.createNotificationChannel(NotificationChannel("flux_alarm_ringing", "old", NotificationManager.IMPORTANCE_LOW))

        AlarmChannels(context).ensure()

        assertNull(manager.getNotificationChannel("flux_alarm_ringing"))
        val ringing = manager.getNotificationChannel(AlarmChannels.RINGING)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, ringing.importance)
        assertEquals(Notification.VISIBILITY_PUBLIC, ringing.lockscreenVisibility)
    }
}
