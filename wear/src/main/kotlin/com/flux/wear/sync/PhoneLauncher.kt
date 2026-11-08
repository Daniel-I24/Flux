package com.flux.wear.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.datalayer.LinkLog
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.wear.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.guava.await

/** Opens something on the phone from the watch: FLUX's store page, or FLUX itself to link this watch. */
@Singleton
class PhoneLauncher @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val gateway: DataLayerGateway,
    private val monitor: PhoneConnectionMonitor,
) {
    suspend fun openStore(): Boolean = open(BuildConfig.PLAY_STORE_URL.toUri(), nodeId = null)

    /** Ajustes > Reloj on the phone, which starts linking this watch (or finishes the sign-in first). */
    suspend fun openLink(): Boolean {
        val phone = monitor.reachablePhoneNode() ?: return false.also { LinkLog.info("watch link: no reachable phone") }
        val watch = gateway.localNodeId() ?: return false
        val uri = Uri.Builder()
            .scheme(SyncProtocol.LINK_SCHEME)
            .authority(SyncProtocol.LINK_HOST)
            .appendQueryParameter(SyncProtocol.LINK_WATCH_PARAM, watch)
            .build()
        return open(uri, phone)
    }

    private suspend fun open(uri: Uri, nodeId: String?): Boolean {
        val intent = Intent(Intent.ACTION_VIEW).addCategory(Intent.CATEGORY_BROWSABLE).setData(uri)
        val helper = RemoteActivityHelper(context, ContextCompat.getMainExecutor(context))
        val result = runCatching { helper.startRemoteActivity(intent, nodeId).await() }
        LinkLog.info("watch open on phone scheme=${uri.scheme} ok=${result.isSuccess}")
        return result.isSuccess
    }
}
