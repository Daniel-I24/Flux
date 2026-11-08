package com.flux.mobile.sound

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * When a preview starts and stops: one at a time, play again = stop, choosing another tone or
 * leaving the screen stops it. Pure (no MediaPlayer), so these rules are tested on the JVM.
 */
class PreviewToggle<T>(private val start: (T) -> Unit, private val halt: () -> Unit) {
    private val current = MutableStateFlow<T?>(null)
    val playing: StateFlow<T?> = current.asStateFlow()

    fun toggle(item: T) {
        val same = current.value == item
        stop()
        if (!same) {
            current.value = item
            start(item)
        }
    }

    /** The tone ended on its own. */
    fun finished(item: T) {
        if (current.value == item) current.value = null
    }

    /** Choosing a tone or leaving the picker stops whatever is playing. */
    fun stop() {
        if (current.value != null) halt()
        current.value = null
    }
}
