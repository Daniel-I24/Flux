package com.flux.mobile.feature.main

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.flux.mobile.R

enum class MainTab(@param:StringRes val label: Int, val icon: ImageVector) {
    HOME(R.string.tab_home, Icons.Rounded.Home),
    NOTES(R.string.tab_notes, Icons.Rounded.Notes),
    CATEGORIES(R.string.tab_categories, Icons.Rounded.Category),
    SETTINGS(R.string.tab_settings, Icons.Rounded.Settings),
}
