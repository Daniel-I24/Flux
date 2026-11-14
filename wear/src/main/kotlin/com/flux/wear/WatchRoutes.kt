package com.flux.wear

object WatchRoutes {
    const val HOME = "home"
    const val DICTATE = "capture/voice"

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
