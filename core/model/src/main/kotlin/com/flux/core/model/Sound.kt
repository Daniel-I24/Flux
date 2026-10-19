package com.flux.core.model

import kotlinx.serialization.Serializable

/** Alarm sound: one bundled in the app (res/raw) or a system ringtone URI. */
@Serializable
sealed interface SoundRef {
    @Serializable
    data class Bundled(val sound: BundledSound) : SoundRef

    @Serializable
    data class System(val uri: String, val title: String) : SoundRef
}

@Serializable
enum class BundledSound { CHIME, PULSE, BELL, SOFT }

@Serializable
enum class VibrationPattern(val timingsMs: LongArray) {
    SHORT(longArrayOf(0, 120)),
    DOUBLE(longArrayOf(0, 120, 100, 120)),
    LONG(longArrayOf(0, 500)),
    HEARTBEAT(longArrayOf(0, 90, 120, 90, 400, 90, 120, 90)),
    OFF(longArrayOf()),
}
