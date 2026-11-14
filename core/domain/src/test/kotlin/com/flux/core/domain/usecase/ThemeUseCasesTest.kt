package com.flux.core.domain.usecase

import com.flux.core.domain.FakeSettingsRepository
import com.flux.core.domain.FixedClock
import com.flux.core.model.ThemePalette
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeUseCasesTest {
    private val settings = FakeSettingsRepository()
    private val clock = FixedClock(nowMillis = 1_000)
    private val update = UpdateSettingsUseCase(settings)

    @Test
    fun `picking a theme stamps it so the newest choice wins between devices`() = runTest {
        SetThemePaletteUseCase(update, clock)(ThemePalette.NIGHT_JUNGLE)
        assertEquals(ThemePalette.NIGHT_JUNGLE, settings.current.palette)
        assertEquals(1_000L, settings.current.paletteChangedAt)

        val remote = ApplyRemotePaletteUseCase(update)
        remote(ThemePalette.ANDEAN_SUNSET, changedAt = 500)
        assertEquals(ThemePalette.NIGHT_JUNGLE, settings.current.palette)
        remote(ThemePalette.ANDEAN_SUNSET, changedAt = 2_000)
        assertEquals(ThemePalette.ANDEAN_SUNSET, settings.current.palette)
        assertEquals(2_000L, settings.current.paletteChangedAt)
    }
}
