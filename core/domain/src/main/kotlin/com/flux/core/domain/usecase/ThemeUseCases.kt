package com.flux.core.domain.usecase

import com.flux.core.domain.port.Clock
import com.flux.core.model.ThemePalette
import javax.inject.Inject

/** The user picked a theme here: stamped, so the newest choice wins between phone and watch. */
class SetThemePaletteUseCase @Inject constructor(private val updateSettings: UpdateSettingsUseCase, private val clock: Clock) {
    suspend operator fun invoke(palette: ThemePalette) = updateSettings { it.copy(palette = palette, paletteChangedAt = clock.now()) }
}

/** A theme picked on the watch: applied only if it is newer than the phone's own choice. */
class ApplyRemotePaletteUseCase @Inject constructor(private val updateSettings: UpdateSettingsUseCase) {
    suspend operator fun invoke(palette: ThemePalette, changedAt: Long) = updateSettings { current ->
        if (changedAt > current.paletteChangedAt) current.copy(palette = palette, paletteChangedAt = changedAt) else current
    }
}
