package com.flux.mobile.link

import com.flux.core.model.WatchLink
import com.flux.core.sync.datalayer.NodeInfo

/** A watch the phone can see: [hasApp] = FLUX is installed on it, so it can be linked right away. */
data class WatchCandidate(val nodeId: String, val displayName: String, val hasApp: Boolean)

data class WatchLinkView(val link: WatchLink, val candidates: List<WatchCandidate>)

/** Turns what the Data Layer sees into the state of Ajustes > Reloj. Pure, so every case is tested. */
object WatchLinkRules {
    /**
     * @param linkedId watch stored in the account (Firestore), even if it is not around right now:
     * a linked watch that is off or out of range is still "linked", so it can be unlinked or replaced.
     */
    fun resolve(linkedId: String?, withApp: List<NodeInfo>, connected: List<NodeInfo>): WatchLinkView {
        val candidates = (
            withApp.map { WatchCandidate(it.id, it.displayName, hasApp = true) } +
                connected.filter { node -> withApp.none { it.id == node.id } }.map { WatchCandidate(it.id, it.displayName, hasApp = false) }
            )
            .distinctBy { it.nodeId }
        val linkedName = (withApp + connected).firstOrNull { it.id == linkedId }?.displayName.orEmpty()
        val link = when {
            linkedId != null -> WatchLink.Linked(linkedId, linkedName, connected = connected.any { it.id == linkedId })
            withApp.isNotEmpty() -> withApp.first().let { WatchLink.Available(it.id, it.displayName) }
            connected.isNotEmpty() -> connected.first().let { WatchLink.AppMissingOnWatch(it.id, it.displayName) }
            else -> WatchLink.NoWatchFound
        }
        return WatchLinkView(link, candidates.filter { it.nodeId != linkedId })
    }
}
