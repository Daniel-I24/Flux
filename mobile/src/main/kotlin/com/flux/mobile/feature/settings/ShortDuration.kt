package com.flux.mobile.feature.settings

/** Alarm length as a short label, consistent with Posponer ("5 min"): under a minute in seconds. */
sealed interface ShortDuration {
    data class Seconds(val value: Int) : ShortDuration

    data class Minutes(val value: Int) : ShortDuration

    companion object {
        private const val SECONDS_PER_MINUTE = 60

        fun of(seconds: Int): ShortDuration =
            if (seconds < SECONDS_PER_MINUTE ||
                seconds % SECONDS_PER_MINUTE != 0
            ) {
                Seconds(seconds)
            } else {
                Minutes(seconds / SECONDS_PER_MINUTE)
            }
    }
}
