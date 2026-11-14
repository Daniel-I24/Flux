package com.flux.wear

import android.net.Uri
import com.flux.wear.feature.capture.CaptureViewModel

object WatchRoutes {
    const val HOME = "home"
    const val DICTATE = "capture/voice"

    /** Dictation from a category: the note is saved in it. */
    const val DICTATE_IN = "$DICTATE/{${CaptureViewModel.CATEGORY_ARG}}"

    fun dictateIn(categoryId: String) = "$DICTATE/${Uri.encode(categoryId)}"

    /** Debug builds only: typing notes on emulators without a microphone. */
    const val TYPE = "capture/keyboard"
    const val AGENDA = "agenda"
    const val OVERDUE = "overdue"
    const val CATEGORIES = "categories"
    const val SETTINGS = "settings"
    const val TUTORIAL = "tutorial"

    /** Permissions, battery and the double press of the button (after the tutorial and from Ajustes). */
    const val SETUP = "setup"
}
