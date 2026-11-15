package com.flux.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.flux.core.model.ThemePalette
import org.junit.Assert.assertTrue
import org.junit.Test

/** WCAG 2.x: text needs 4.5:1 (AA); buttons and outlines against the surface need 3:1. */
class FluxPaletteContrastTest {
    private fun ratio(a: Color, b: Color): Double {
        val (light, dark) = listOf(a.luminance() + OFFSET, b.luminance() + OFFSET).sortedDescending()
        return (light / dark).toDouble()
    }

    private fun FluxRoles.textPairs() = mapOf(
        "primary" to (primary to onPrimary),
        "primaryContainer" to (primaryContainer to onPrimaryContainer),
        "secondary" to (secondary to onSecondary),
        "secondaryContainer" to (secondaryContainer to onSecondaryContainer),
        "tertiary" to (tertiary to onTertiary),
        "tertiaryContainer" to (tertiaryContainer to onTertiaryContainer),
        "surface" to (surface to onSurface),
        "background" to (background to onSurface),
        "surfaceVariant" to (surfaceVariant to onSurfaceVariant),
        "surface/variant text" to (surface to onSurfaceVariant),
        "containerHigh" to (containerHigh to onSurface),
        "error" to (error to onError),
        // The home greeting sits on a primary → tertiary gradient.
        "hero gradient end" to (tertiary to onPrimary),
    )

    @Test
    fun `every text pair of both themes passes AA in light and dark`() {
        ThemePalette.entries.forEach { palette ->
            val variants = palette.roles().let { mapOf("light" to it.light, "dark" to it.dark) }
            variants.forEach { (variant, roles) ->
                roles.textPairs().forEach { (name, pair) ->
                    val value = ratio(pair.first, pair.second)
                    assertTrue("$palette $variant $name = $value", value >= TEXT_AA)
                }
                assertTrue("$palette $variant primary on surface", ratio(roles.primary, roles.surface) >= UI_AA)
            }
        }
    }

    @Test
    fun `the watch uses the dark variant on black and stays AA`() {
        ThemePalette.entries.forEach { palette ->
            val dark = palette.roles().dark
            listOf(dark.onSurface, dark.onSurfaceVariant, dark.primary).forEach { text ->
                assertTrue("$palette $text on black", ratio(text, FluxColorTokens.Black) >= TEXT_AA)
            }
        }
    }

    private companion object {
        const val OFFSET = 0.05f
        const val TEXT_AA = 4.5
        const val UI_AA = 3.0
    }
}
