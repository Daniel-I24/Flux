package com.flux.mobile.launch

import android.content.Context
import android.content.Intent
import com.flux.mobile.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** How a capture was asked from outside the app: dictating right away, or typing. */
enum class CaptureLaunch { DICTATE, TYPE }

/** A pending outside capture and how to mark it handled (passed down from the activity). */
data class CaptureRequest(val launch: CaptureLaunch? = null, val consume: () -> Unit = {})

/**
 * Launcher shortcuts and the quick settings tile open FLUX with an action; it waits here until
 * the notes tab opens the capture sheet (after onboarding, if it is not finished yet).
 */
@Singleton
class CaptureLaunchRequests @Inject constructor() {
    private val pending = MutableStateFlow<CaptureLaunch?>(null)
    val request: StateFlow<CaptureLaunch?> = pending.asStateFlow()

    fun handle(intent: Intent?) {
        parse(intent?.action)?.let { pending.value = it }
    }

    fun consume() {
        pending.value = null
    }

    companion object {
        const val ACTION_DICTATE = "com.flux.mobile.action.DICTATE"
        const val ACTION_NEW_NOTE = "com.flux.mobile.action.NEW_NOTE"

        fun parse(action: String?): CaptureLaunch? = when (action) {
            ACTION_DICTATE -> CaptureLaunch.DICTATE
            ACTION_NEW_NOTE -> CaptureLaunch.TYPE
            else -> null
        }

        fun intent(context: Context, action: String): Intent = Intent(context, MainActivity::class.java)
            .setAction(action)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
}
