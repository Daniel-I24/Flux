package com.flux.core.data.alarm

import android.util.Log

/**
 * Alarm diagnostics for `adb logcat -s FluxAlarm`, kept in release like FluxLink: R8 strips
 * Log.v/d/i, not Log.println. Only states and reasons, never note titles or texts.
 */
object AlarmLog {
    const val TAG = "FluxAlarm"

    fun info(message: String) {
        Log.println(Log.INFO, TAG, message)
    }
}
