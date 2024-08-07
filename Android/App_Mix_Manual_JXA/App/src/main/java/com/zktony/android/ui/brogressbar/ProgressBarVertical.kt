package com.zktony.android.ui.brogressbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 竖形进度条
 * progress 进度
 * color    进度颜色
 * backgroundColor  底色
 */
@Composable
fun VerticalProgressBar(
    progress: MutableState<Float>,
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    backgroundColor: Color = Color.Black,
    size: Size = Size(width = 50f, height = 100f),
    strokeSize: Float = 1f,
    strokeColor: Color = Color.Blue
) {
    Canvas(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .border(width = strokeSize.dp, color = strokeColor)
    ) {
        // Progress made
        drawRect(
            color = color,
            size = Size(size.width.dp.toPx(), height = (progress.value * size.height).dp.toPx()),
            topLeft = Offset(0.dp.toPx(), ((1 - progress.value) * size.height).dp.toPx())
        )
        // background
        drawRect(
            color = backgroundColor,
            size = Size(
                width = size.width.dp.toPx(),
                height = ((1 - progress.value) * size.height).dp.toPx()
            ),
        )
    }
}