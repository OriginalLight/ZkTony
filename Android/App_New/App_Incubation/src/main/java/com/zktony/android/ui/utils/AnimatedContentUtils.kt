package com.zktony.android.ui.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@ExperimentalAnimationApi
@Composable
fun <T> AnimatedContent(
    targetState: T,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<T>.() -> ContentTransform = {
        (fadeIn() + expandVertically()).togetherWith(fadeOut() + shrinkVertically())
    },
    contentAlignment: Alignment = Alignment.TopStart,
    content: @Composable() AnimatedVisibilityScope.(targetState: T) -> Unit
) {
    val transition = updateTransition(targetState = targetState, label = "AnimatedContent")
    transition.AnimatedContent(
        modifier, transitionSpec, contentAlignment, content = content
    )
}