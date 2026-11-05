package com.flux.wear.feature.home

/** Navigation callbacks offered by the home screen; [type] exists only in debug builds. */
data class HomeActions(
    val dictate: () -> Unit,
    val type: (() -> Unit)?,
    val agenda: () -> Unit,
    val overdue: () -> Unit,
    val categories: () -> Unit,
    val settings: () -> Unit,
    val tutorial: () -> Unit,
)
