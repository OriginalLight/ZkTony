package com.zktony.android.ui.brogressbar

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 横向进度条2
 */
@Composable
fun ProgressBar(
    modifier: Modifier,
    progress: Float,
    color: Color,
    cornerRadius: Dp,
    backgroundColor: Color,
) {
    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = backgroundColor,
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius)) // 裁剪矩形区域为圆角矩形，将超出圆角矩形的部分绘制去掉
            .drawWithContent {
                drawContent() // 先绘制内容后绘制自定义图形，这样我们绘制的图形将显示在内容区域上方
                val progressWidth = drawContext.size.width * progress
                drawRect(
                    color = color,
                    size = drawContext.size.copy(width = progressWidth),
                )
            },
        content = {}
    )
}
