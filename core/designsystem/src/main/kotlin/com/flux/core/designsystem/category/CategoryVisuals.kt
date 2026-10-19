package com.flux.core.designsystem.category

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.flux.core.designsystem.R
import com.flux.core.model.Category
import com.flux.core.model.CategoryIcon

/** Single mapping from the model's icon enum to vectors (used by phone and watch). */
fun CategoryIcon.toImageVector(): ImageVector = when (this) {
    CategoryIcon.TASK -> Icons.Rounded.Checklist
    CategoryIcon.SCHOOL -> Icons.Rounded.School
    CategoryIcon.IDEA -> Icons.Rounded.Lightbulb
    CategoryIcon.MEAL -> Icons.Rounded.Restaurant
    CategoryIcon.HEALTH -> Icons.Rounded.LocalHospital
    CategoryIcon.SHOPPING -> Icons.Rounded.ShoppingCart
    CategoryIcon.ALARM -> Icons.Rounded.NotificationsActive
    CategoryIcon.INFO -> Icons.Rounded.Info
    CategoryIcon.WORK -> Icons.Rounded.Work
    CategoryIcon.HOME -> Icons.Rounded.Home
    CategoryIcon.SPORT -> Icons.Rounded.FitnessCenter
    CategoryIcon.MONEY -> Icons.Rounded.AttachMoney
    CategoryIcon.TRAVEL -> Icons.Rounded.Flight
    CategoryIcon.STAR -> Icons.Rounded.Star
    CategoryIcon.HEART -> Icons.Rounded.Favorite
    CategoryIcon.MUSIC -> Icons.Rounded.MusicNote
}

/** Spoken name of each icon for TalkBack in the icon picker. */
@StringRes
fun CategoryIcon.labelRes(): Int = when (this) {
    CategoryIcon.TASK -> R.string.icon_task
    CategoryIcon.SCHOOL -> R.string.icon_school
    CategoryIcon.IDEA -> R.string.icon_idea
    CategoryIcon.MEAL -> R.string.icon_meal
    CategoryIcon.HEALTH -> R.string.icon_health
    CategoryIcon.SHOPPING -> R.string.icon_shopping
    CategoryIcon.ALARM -> R.string.icon_alarm
    CategoryIcon.INFO -> R.string.icon_info
    CategoryIcon.WORK -> R.string.icon_work
    CategoryIcon.HOME -> R.string.icon_home
    CategoryIcon.SPORT -> R.string.icon_sport
    CategoryIcon.MONEY -> R.string.icon_money
    CategoryIcon.TRAVEL -> R.string.icon_travel
    CategoryIcon.STAR -> R.string.icon_star
    CategoryIcon.HEART -> R.string.icon_heart
    CategoryIcon.MUSIC -> R.string.icon_music
}

val Category.color: Color get() = Color(colorArgb)
