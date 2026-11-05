package com.flux.wear.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

/**
 * Safe padding for full-screen content: round screens lose their corners, so they get more
 * horizontal room (≈14 % of the width); square and rectangular screens only need a small margin.
 */
@Composable
fun rememberWatchInsets(): PaddingValues {
    val configuration = LocalConfiguration.current
    val round = configuration.isScreenRound
    val width = configuration.screenWidthDp
    val height = configuration.screenHeightDp
    val horizontal = width * if (round) ROUND_HORIZONTAL else SQUARE_MARGIN
    val vertical = height * if (round) ROUND_VERTICAL else SQUARE_MARGIN
    return PaddingValues(horizontal = horizontal.dp, vertical = vertical.dp)
}

private const val ROUND_HORIZONTAL = 0.14f
private const val ROUND_VERTICAL = 0.12f
private const val SQUARE_MARGIN = 0.06f
