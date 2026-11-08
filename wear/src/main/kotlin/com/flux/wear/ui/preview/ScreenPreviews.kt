package com.flux.wear.ui.preview

import androidx.compose.runtime.Composable
import com.flux.core.model.ClassifierEngine
import com.flux.core.model.DefaultCategory
import com.flux.core.model.DeviceSource
import com.flux.core.model.Note
import com.flux.wear.data.PhoneState
import com.flux.wear.feature.agenda.AgendaContent
import com.flux.wear.feature.agenda.AgendaUiState
import com.flux.wear.feature.capture.CaptureFailure
import com.flux.wear.feature.capture.ErrorContent
import com.flux.wear.feature.capture.ListeningContent
import com.flux.wear.feature.capture.SavedContent
import com.flux.wear.feature.categories.CategoriesContent
import com.flux.wear.feature.link.LinkConfirmContent
import com.flux.wear.feature.overdue.OverdueActions
import com.flux.wear.feature.overdue.OverdueContent
import com.flux.wear.feature.overdue.OverdueUiState
import com.flux.wear.feature.settings.WatchSettingsActions
import com.flux.wear.feature.settings.WatchSettingsContent
import com.flux.wear.feature.settings.WatchSettingsUiState
import com.flux.wear.sync.PendingLink
import com.flux.wear.ui.theme.FluxWearTheme

private val categories = DefaultCategory.entries.map { it.toCategory() }

private fun note(id: String, title: String, inMinutes: Long) = Note(
    id = id,
    text = title,
    title = title,
    categoryId = DefaultCategory.HEALTH.id,
    dueAt = System.currentTimeMillis() + inMinutes * 60_000L,
    requiresAlarm = true,
    source = DeviceSource.WATCH,
    engine = ClassifierEngine.LOCAL,
    createdAt = 0,
    updatedAt = 0,
)

@FluxWearPreviews
@Composable
internal fun SettingsPreview() = FluxWearTheme {
    WatchSettingsContent(
        WatchSettingsUiState(phone = PhoneState(userName = "Ana"), linkedAccount = "account"),
        WatchSettingsActions({}, {}, {}, {}, {}, {}),
    )
}

@FluxWearPreviews
@Composable
internal fun AgendaPreview() = FluxWearTheme {
    val today = listOf(note("a", "Cita odontológica con la doctora Pérez", 90))
    AgendaContent(AgendaUiState(today = today, categories = categories.associateBy { it.id }, loading = false)) {}
}

@FluxWearPreviews
@Composable
internal fun OverduePreview() = FluxWearTheme {
    OverdueContent(
        OverdueUiState(listOf(note("o", "Llamar al banco", -30)), categories.associateBy { it.id }, loading = false),
        OverdueActions({}, {}, {}, {}),
        initiallySelected = "o",
    )
}

@FluxWearPreviews
@Composable
internal fun CategoriesPreview() = FluxWearTheme { CategoriesContent(categories) {} }

@FluxWearPreviews
@Composable
internal fun LinkConfirmPreview() = FluxWearTheme {
    LinkConfirmContent(PendingLink("123456", "account", "Ana María", "phone")) {}
}

@FluxWearPreviews
@Composable
internal fun ListeningPreview() = FluxWearTheme { ListeningContent(level = { 0.4f }, partial = "comprar pan mañana a las 8", onDone = {}) }

@FluxWearPreviews
@Composable
internal fun CaptureErrorPreview() = FluxWearTheme {
    ErrorContent(CaptureFailure.NO_NETWORK, onRetry = {}, onOpenSettings = {}, onType = null)
}

@FluxWearPreviews
@Composable
internal fun SavedPreview() = FluxWearTheme { SavedContent(queued = true) }
