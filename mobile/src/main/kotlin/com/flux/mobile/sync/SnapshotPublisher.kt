package com.flux.mobile.sync

import com.flux.core.domain.port.Clock
import com.flux.core.domain.repository.CategoryRepository
import com.flux.core.domain.repository.OnboardingStep
import com.flux.core.domain.usecase.AccountIds
import com.flux.core.domain.usecase.ObserveAccountLinkUseCase
import com.flux.core.domain.usecase.ObserveNoteSectionsUseCase
import com.flux.core.domain.usecase.ObserveOnboardingStepUseCase
import com.flux.core.domain.usecase.ObserveProfileUseCase
import com.flux.core.domain.usecase.ObserveSessionUseCase
import com.flux.core.domain.usecase.ObserveSettingsUseCase
import com.flux.core.model.AppSettings
import com.flux.core.model.Category
import com.flux.core.model.Note
import com.flux.core.model.SoundRef
import com.flux.core.model.UserProfile
import com.flux.core.sync.datalayer.DataLayerGateway
import com.flux.core.sync.protocol.AccountState
import com.flux.core.sync.protocol.QuietWindow
import com.flux.core.sync.protocol.SyncProtocol
import com.flux.core.sync.protocol.WatchSnapshot
import com.flux.mobile.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Publishes what the watch needs to work offline (agenda, categories, settings, account
 * state) through DataClient whenever any of it changes. Debounced to save battery.
 */
@Singleton
class SnapshotPublisher @Inject constructor(
    private val gateway: DataLayerGateway,
    private val clock: Clock,
    private val observeSession: ObserveSessionUseCase,
    private val observeStep: ObserveOnboardingStepUseCase,
    private val observeProfile: ObserveProfileUseCase,
    private val observeSettings: ObserveSettingsUseCase,
    private val observeLink: ObserveAccountLinkUseCase,
    private val observeSections: ObserveNoteSectionsUseCase,
    private val categories: CategoryRepository,
) {
    @OptIn(FlowPreview::class)
    fun start(scope: CoroutineScope) {
        val account = combine(observeSession(), observeStep(), observeProfile(), observeLink()) { session, step, profile, link ->
            AccountPart(session?.uid, step, profile, link?.watchNodeId != null)
        }
        // Upcoming and recently overdue reminders: the watch shows "Próximo" and "Vencidos" offline.
        val agenda = observeSections().map { it.overdue.take(OVERDUE_SIZE).reversed() + it.upcoming.take(AGENDA_SIZE) }
        scope.launch {
            combine(account, observeSettings(), categories.observeCategories(), agenda, ::toSnapshot)
                .distinctUntilChanged()
                .debounce(DEBOUNCE_MS)
                .collect { snapshot -> gateway.putSnapshot(snapshot.copy(generatedAt = clock.now())) }
        }
    }

    private fun toSnapshot(part: AccountPart, settings: AppSettings, categoryList: List<Category>, agenda: List<Note>) = WatchSnapshot(
        version = SyncProtocol.VERSION,
        generatedAt = 0L,
        accountState = when {
            part.uid == null -> AccountState.SIGNED_OUT
            part.step != OnboardingStep.DONE -> AccountState.ONBOARDING
            else -> AccountState.READY
        },
        linkedAccountId = part.uid?.takeIf { part.watchLinked }?.let(AccountIds::fromUid),
        userName = part.profile?.displayName,
        categories = categoryList.map { it.copy(sound = it.sound.forWatch()) },
        agenda = if (part.uid == null) emptyList() else agenda.map { it.copy(sound = it.sound.forWatch()) },
        snoozeMinutes = settings.snoozeMinutes,
        vibration = settings.vibration,
        quietHours = part.profile?.routine?.takeIf { settings.quietHoursEnabled }?.let { QuietWindow(it.sleep.minutes, it.wakeUp.minutes) },
        alarmDurationSeconds = settings.alarmDurationSeconds,
        phoneAppVersion = BuildConfig.VERSION_CODE,
    )

    /** Custom tone files live only on the phone (and v1.0 watches cannot decode them). */
    private fun SoundRef?.forWatch(): SoundRef? = takeUnless { it is SoundRef.Custom }

    private data class AccountPart(
        val uid: String?,
        val step: OnboardingStep,
        val profile: UserProfile?,
        val watchLinked: Boolean,
    )

    private companion object {
        const val AGENDA_SIZE = 30
        const val OVERDUE_SIZE = 10
        const val DEBOUNCE_MS = 500L
    }
}
