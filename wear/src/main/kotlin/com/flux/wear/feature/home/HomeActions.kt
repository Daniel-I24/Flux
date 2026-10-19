package com.flux.wear.feature.home

/** Navigation callbacks offered by the home screen. */
data class HomeActions(
    val dictate: () -> Unit,
    val type: () -> Unit,
    val agenda: () -> Unit,
    val categories: () -> Unit,
    val settings: () -> Unit,
)
