package com.flux.core.sync.datalayer

import android.util.Log

/**
 * Link diagnostics for `adb logcat -s FluxLink`. Kept in release on purpose (N-075): R8 strips
 * Log.v/d/i, not Log.println. Only states and counts: never node ids, names, codes or accounts.
 */
object LinkLog {
    const val TAG = "FluxLink"

    fun info(message: String) {
        Log.println(Log.INFO, TAG, message)
    }
}
