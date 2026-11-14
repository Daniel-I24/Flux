package com.flux.mobile.feature.capture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flux.core.data.speech.VoiceErrorPolicy
import com.flux.core.designsystem.theme.FluxSpacing
import com.flux.mobile.R

/** "Nueva nota" by voice or text: the microphone permission is asked only when the mic is tapped. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSheet(onDismiss: () -> Unit, onAlarmSaved: (Long) -> Unit = {}, viewModel: PhoneCaptureViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.phase) {
        if (state.phase != CapturePhase.SAVED) return@LaunchedEffect
        state.classification?.takeIf { it.requiresAlarm }?.dueAt?.let(onAlarmSaved)
        onDismiss()
    }
    DisposableEffect(viewModel) { onDispose(viewModel::reset) }
    val onMic = rememberMicAction(viewModel)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.padding(horizontal = FluxSpacing.lg).padding(bottom = FluxSpacing.lg).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(FluxSpacing.md),
        ) {
            Text(stringResource(R.string.notes_new), style = MaterialTheme.typography.titleLarge)
            when (state.phase) {
                CapturePhase.EDITING, CapturePhase.LISTENING -> CaptureComposer(state, viewModel, onMic, onOpenSettings = {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri()))
                })
                CapturePhase.CLASSIFYING -> Box(Modifier.fillMaxWidth().padding(FluxSpacing.xl), Alignment.Center) {
                    CircularProgressIndicator()
                }
                CapturePhase.PREVIEW, CapturePhase.SAVED -> state.classification?.let { CapturePreview(state, it, categories, viewModel) }
            }
        }
    }
}

/** Asks for the microphone if needed, then listens in FLUX or, when FLUX cannot, in the system dictation screen. */
@Composable
private fun rememberMicAction(viewModel: PhoneCaptureViewModel): () -> Unit {
    val context = LocalContext.current
    val system = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val heard = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
        if (result.resultCode == Activity.RESULT_OK) viewModel.heardFromSystem(heard)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val openSystem: () -> Unit = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            .putExtra(RecognizerIntent.EXTRA_LANGUAGE, VoiceErrorPolicy.PRIMARY_LANGUAGE)
        val failed = runCatching { system.launch(intent) }.isFailure
        viewModel.systemDictationHandled(failed)
    }
    // FLUX's own recognizer could not run (no Spanish, no service): the system dictation is next.
    LaunchedEffect(state.systemDictation) { if (state.systemDictation != null) openSystem() }
    val listen: () -> Unit = {
        if (viewModel.canListenInApp()) viewModel.listen() else viewModel.requestSystemDictation()
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) listen() else viewModel.permissionDenied()
    }
    return {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        if (granted) listen() else permission.launch(Manifest.permission.RECORD_AUDIO)
    }
}
