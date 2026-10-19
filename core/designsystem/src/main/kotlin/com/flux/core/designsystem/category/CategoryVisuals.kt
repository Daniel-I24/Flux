package com.flux.core.designsystem.category

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

val Category.color: Color get() = Color(colorArgb)
