package com.flux.mobile.link

import android.content.Intent
import android.net.Uri
import com.flux.core.sync.datalayer.LinkLog
import com.flux.core.sync.protocol.SyncProtocol
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** The watch asked to be linked ("Vincular" on the watch): which watch, if it said so. */
data class LinkLaunch(val watchNodeId: String?)

/**
 * Holds a pending flux://link request until Ajustes > Reloj handles it. If the user still has to
 * sign in, it waits: onboarding ends in the main screen, which then opens Ajustes.
 */
@Singleton
class LinkLaunchRequests @Inject constructor() {
    private val pending = MutableStateFlow<LinkLaunch?>(null)
    val request: StateFlow<LinkLaunch?> = pending.asStateFlow()

    fun handle(intent: Intent?) {
        parse(intent?.data)?.let {
            LinkLog.info("phone deep link received watch=${it.watchNodeId != null}")
            pending.value = it
        }
    }

    fun consume() {
        pending.value = null
    }

    companion object {
        private const val MAX_NODE_ID = 64

        /** Only flux://link is accepted; a malformed watch id is dropped (the user picks the watch instead). */
        fun parse(uri: Uri?): LinkLaunch? {
            if (uri?.scheme != SyncProtocol.LINK_SCHEME || uri.host != SyncProtocol.LINK_HOST) return null
            val watch = uri.getQueryParameter(SyncProtocol.LINK_WATCH_PARAM)
                ?.takeIf { id -> id.length <= MAX_NODE_ID && id.all { it.isLetterOrDigit() || it == '-' || it == '_' } }
            return LinkLaunch(watch)
        }
    }
}
