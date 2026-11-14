package com.flux.mobile.alarm

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.flux.core.data.alarm.AlarmLog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * With the phone unlocked and in use, Android turns a full-screen intent into a heads-up that
 * hides after a few seconds. With "Aparecer encima" granted, FLUX opens the alarm screen itself
 * over the app in use, like an incoming call. Without it, the insistent notification remains.
 */
class AlarmPresenter @Inject constructor(@ApplicationContext private val context: Context) {
    fun present(noteId: String) {
        val overlay = Settings.canDrawOverlays(context)
        val interactive = context.getSystemService(PowerManager::class.java).isInteractive
        AlarmLog.info("phone alarm present overlay=$overlay interactive=$interactive fullScreen=${canUseFullScreen()}")
        if (!overlay) return
        runCatching { context.startActivity(AlarmActivity.intent(context, noteId)) }
            .onFailure { AlarmLog.info("phone alarm screen blocked=${it.javaClass.simpleName}") }
    }

    private fun canUseFullScreen(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
        context.getSystemService(NotificationManager::class.java).canUseFullScreenIntent()
}
