package com.flux.mobile.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.launch

/**
 * One-shot events shown in a snackbar. Consuming the event changes the effect's key and
 * restarts it, which used to cancel the snackbar it had just shown; the snackbar now runs in
 * the screen's scope, so it stays (and "Deshacer" still works).
 */
@Composable
fun <T : Any> SnackbarEventEffect(event: T?, onConsumed: () -> Unit, show: suspend (T) -> Unit) {
    val scope = rememberCoroutineScope()
    val currentShow by rememberUpdatedState(show)
    LaunchedEffect(event) {
        val current = event ?: return@LaunchedEffect
        onConsumed()
        scope.launch { currentShow(current) }
    }
}
