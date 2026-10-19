package com.flux.core.sync.protocol

/** Wire constants shared by both apps. Bump [VERSION] on any incompatible change. */
object SyncProtocol {
    const val VERSION = 1
    const val MESSAGE_PATH = "/flux/v1/message"
    const val SNAPSHOT_PATH = "/flux/v1/snapshot"
    const val SNAPSHOT_KEY = "payload"
    const val PHONE_CAPABILITY = "flux_phone_app"
    const val WATCH_CAPABILITY = "flux_watch_app"

    /** Data Layer messages should stay small; anything bigger is rejected. */
    const val MAX_MESSAGE_BYTES = 16 * 1024
    const val MAX_SNAPSHOT_BYTES = 90 * 1024
    const val LINK_CODE_LENGTH = 6
}
