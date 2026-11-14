package com.flux.mobile.feature.notes

import com.flux.mobile.launch.CaptureLaunch

/**
 * A category asked by another tab (Inicio), and optionally a capture to open right away (launcher
 * shortcut or quick settings); [token] changes on every request so it is applied once.
 */
data class NotesRequest(val categoryId: String?, val token: Long, val capture: CaptureLaunch? = null)
