package com.flux.core.data.permissions

/** Everything FLUX may need so dictation works and alarms ring on time, on the phone and on the watch. */
enum class AppPermission {
    MICROPHONE,
    NOTIFICATIONS,

    /** Android 12 only: from Android 13 USE_EXACT_ALARM grants it at install. */
    EXACT_ALARMS,

    /** Android 14+: the alarm over the lock screen. */
    FULL_SCREEN,

    /** Phone only: the alarm over the app in use, like an incoming call. */
    OVERLAY,

    /** No battery restrictions, so the system does not freeze FLUX. */
    BATTERY,

    /** Brand screens (Xiaomi, vivo, Oppo, Huawei, Samsung…): start in the background. */
    AUTOSTART,

    /** Brand screens (Xiaomi, vivo, Oppo): pop-up windows and showing on the lock screen. */
    BRAND_POPUPS,
}

/** [UNKNOWN]: a brand screen Android cannot report on; the user checks it there. */
enum class PermissionStatus { GRANTED, PENDING, UNKNOWN }
