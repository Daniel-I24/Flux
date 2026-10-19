package com.flux.core.domain.usecase

import com.flux.core.domain.repository.OnboardingRepository
import com.flux.core.domain.repository.OnboardingStep
import com.flux.core.domain.repository.ProfileRepository
import com.flux.core.domain.repository.SettingsRepository
import com.flux.core.model.AppError
import com.flux.core.model.AppSettings
import com.flux.core.model.DailyRoutine
import com.flux.core.model.FluxResult
import com.flux.core.model.Occupation
import com.flux.core.model.UserProfile
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

enum class ProfileField { NAME, AGE }

class ObserveProfileUseCase @Inject constructor(private val profiles: ProfileRepository) {
    operator fun invoke(): Flow<UserProfile?> = profiles.observeProfile()
}

/** Minimal, validated profile: name (required), age (optional, 5-120) and occupation. */
class SaveProfileUseCase @Inject constructor(private val profiles: ProfileRepository) {
    fun validate(name: String, age: String): Set<ProfileField> = buildSet {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > UserProfile.MAX_NAME_LENGTH) add(ProfileField.NAME)
        if (age.isNotBlank() && age.trim().toIntOrNull()?.let { it in UserProfile.AGE_RANGE } != true) add(ProfileField.AGE)
    }

    suspend operator fun invoke(name: String, age: String, occupation: Occupation): FluxResult<UserProfile> {
        if (validate(name, age).isNotEmpty()) return FluxResult.Failure(AppError.InvalidInput)
        val current = profiles.getProfile()
        val profile = UserProfile(
            displayName = name.trim(),
            age = age.trim().toIntOrNull(),
            occupation = occupation,
            routine = current?.routine ?: DailyRoutine(),
        )
        profiles.saveProfile(profile)
        return FluxResult.Success(profile)
    }
}

/** Routine is validated so the quiet window and activity blocks make sense. */
class SaveRoutineUseCase @Inject constructor(private val profiles: ProfileRepository) {
    suspend operator fun invoke(routine: DailyRoutine): FluxResult<Unit> {
        val profile = profiles.getProfile() ?: return FluxResult.Failure(AppError.NotFound)
        val blocksValid = routine.activityBlocks.values.all { it.start < it.end }
        if (!blocksValid || routine.wakeUp == routine.sleep) return FluxResult.Failure(AppError.InvalidInput)
        profiles.saveProfile(profile.copy(routine = routine))
        return FluxResult.Success(Unit)
    }
}

class ObserveOnboardingStepUseCase @Inject constructor(private val onboarding: OnboardingRepository) {
    operator fun invoke(): Flow<OnboardingStep> = onboarding.observeStep()
}

class SetOnboardingStepUseCase @Inject constructor(private val onboarding: OnboardingRepository) {
    suspend operator fun invoke(step: OnboardingStep) = onboarding.setStep(step)
}

class ObserveSettingsUseCase @Inject constructor(private val settings: SettingsRepository) {
    operator fun invoke(): Flow<AppSettings> = settings.observeSettings()
}

class UpdateSettingsUseCase @Inject constructor(private val settings: SettingsRepository) {
    suspend operator fun invoke(transform: (AppSettings) -> AppSettings) = settings.update(transform)
}
