package com.zktony.android.ui.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * @author 刘贺贺
 * @date 2023/9/4 14:23
 */
@Composable
fun selectedColor(boolean: Boolean): Color {
    return if (boolean) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
fun randomColor() = Color(
    red = (0..255).random(),
    green = (0..255).random(),
    blue = (0..255).random(),
    alpha = 255
)