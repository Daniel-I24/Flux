package com.flux.core.sync.datalayer

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Play services destroys a WearableListenerService as soon as the event is delivered, while the
 * work it starts (a classification of several seconds, an acknowledgment) must still finish.
 */
@Singleton
class SyncWorkScope @Inject constructor() : CoroutineScope by CoroutineScope(SupervisorJob() + Dispatchers.Default)
