package com.flux.mobile.data.firebase

import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.AccountLinkRepository
import com.flux.core.model.AccountLink
import com.flux.core.model.FluxResult
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * users/{uid} = { displayName, link: { phoneInstallationId, watchNodeId, linkedAt }, updatedAt }.
 * Security rules (firebase/firestore.rules) only let each user read and write their own document.
 */
class FirestoreAccountLinkRepository @Inject constructor(private val clock: Clock) : AccountLinkRepository {
    private val users get() = FirebaseFirestore.getInstance().collection(COLLECTION)

    override fun observeLink(uid: String): Flow<AccountLink?> = callbackFlow {
        val registration = users.document(uid).addSnapshotListener { snapshot, _ -> trySend(snapshot?.toLink()) }
        awaitClose { registration.remove() }
    }

    override suspend fun getLink(uid: String) = firebaseCall { users.document(uid).get().await().toLink() }

    override suspend fun claimPhone(uid: String, installationId: String) =
        write(uid, mapOf("phoneInstallationId" to installationId, "linkedAt" to clock.now()))

    override suspend fun linkWatch(uid: String, watchNodeId: String) = write(uid, mapOf("watchNodeId" to watchNodeId))

    override suspend fun unlinkWatch(uid: String) = write(uid, mapOf("watchNodeId" to null))

    override suspend fun saveProfileSnapshot(uid: String, displayName: String): FluxResult<Unit> = firebaseCall {
        users.document(uid)
            .set(mapOf("displayName" to displayName, "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge())
            .await()
        Unit
    }

    private suspend fun write(uid: String, link: Map<String, Any?>): FluxResult<Unit> = firebaseCall {
        users.document(uid).set(mapOf("link" to link, "updatedAt" to FieldValue.serverTimestamp()), SetOptions.merge()).await()
        Unit
    }

    private fun DocumentSnapshot.toLink(): AccountLink? {
        if (!exists()) return null
        @Suppress("UNCHECKED_CAST")
        val link = get("link") as? Map<String, Any?> ?: return AccountLink(null, null, null)
        return AccountLink(
            phoneInstallationId = link["phoneInstallationId"] as? String,
            watchNodeId = link["watchNodeId"] as? String,
            linkedAt = (link["linkedAt"] as? Number)?.toLong(),
        )
    }

    private companion object {
        const val COLLECTION = "users"
    }
}
