package com.flux.core.designsystem.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/** Page change used by onboarding and tutorials; instant when the user reduced animations. */
fun <S> AnimatedContentTransitionScope<S>.fluxPageTransition(reduced: Boolean): ContentTransform = if (reduced) {
    EnterTransition.None togetherWith ExitTransition.None
} else {
    (fadeIn(tween(FluxMotion.MEDIUM_MS)) + slideInHorizontally(tween(FluxMotion.MEDIUM_MS)) { it / SLIDE_FRACTION }) togetherWith
        (fadeOut(tween(FluxMotion.SHORT_MS)) + slideOutHorizontally(tween(FluxMotion.SHORT_MS)) { -it / SLIDE_FRACTION })
}

private const val SLIDE_FRACTION = 4
