package com.zktony.android.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@ExperimentalAnimationApi
@Composable
fun <T> AnimatedContent(
    targetState: T,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
        fadeIn(
            animationSpec = tween(
                700,
                easing = FastOutSlowInEasing
            )
        ) togetherWith fadeOut(
            animationSpec = tween(
                700,
                easing = FastOutSlowInEasing
            )
        )
    },
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable AnimatedVisibilityScope.(targetState: T) -> Unit
) {
    val transition = updateTransition(targetState = targetState, label = "AnimatedContent")
    transition.AnimatedContent(
        modifier, transitionSpec, contentAlignment, content = content
    )
}