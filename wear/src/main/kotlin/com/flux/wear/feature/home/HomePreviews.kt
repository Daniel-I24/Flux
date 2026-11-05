package com.flux.wear.feature.home

import androidx.compose.runtime.Composable
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import com.flux.core.model.PhoneConnection
import com.flux.wear.feature.alert.AlertScreen
import com.flux.wear.feature.tutorial.WatchTutorialScreen
import com.flux.wear.ui.preview.FluxWearPreviews
import com.flux.wear.ui.theme.FluxWearTheme

private val sampleNote = Note(
    id = "preview",
    text = "cita odontológica hoy a las 4:20 p. m.",
    title = "Cita odontológica",
    categoryId = "health",
    dueAt = System.currentTimeMillis() + 60 * 60 * 1_000L,
    requiresAlarm = true,
    source = DeviceSource.WATCH,
    engine = ClassifierEngine.LOCAL,
    createdAt = 0,
    updatedAt = 0,
)

private val noActions = HomeActions({}, null, {}, {}, {}, {}, {})

@FluxWearPreviews
@Composable
internal fun HomePreview() = FluxWearTheme {
    HomeContent(
        state = HomeUiState(connection = PhoneConnection.Ready("Ana", serverReachable = true), nextReminder = sampleNote, overdueCount = 2),
        actions = noActions,
        onProblem = {},
    )
}

@FluxWearPreviews
@Composable
internal fun AlertPreview() = FluxWearTheme { AlertScreen(note = sampleNote, onComplete = {}, onSnooze = {}) }

@FluxWearPreviews
@Composable
internal fun TutorialPreview() = FluxWearTheme { WatchTutorialScreen(onFinish = {}) }
