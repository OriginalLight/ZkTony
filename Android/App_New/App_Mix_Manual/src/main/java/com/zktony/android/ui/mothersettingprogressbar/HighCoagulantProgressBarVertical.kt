package com.zktony.android.ui.mothersettingprogressbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 低浓度竖形进度条
 * progress 进度
 * color    进度颜色
 * backgroundColor  底色
 */
@Composable
fun HighCoagulantProgressBarVertical(
    progress: Float,
    volume: String,
    concentration: String,
    modifier: Modifier = Modifier,
    color: Color = Color(android.graphics.Color.rgb(136, 196, 254)),
    backgroundColor: Color = Color(245, 245, 245),
    size: Size = Size(width = 102.48f, height = 150.85f),
    strokeSize: Float = 0.1f,
    strokeColor: Color = Color.Blue
) {

    val textDesc = "高浓度"
    val textDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString(textDesc),
        TextStyle(color = Color(18, 95, 202), fontSize = 13.sp)
    )


    val volumeDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("${volume}mL"),
        TextStyle(color = Color(18, 95, 202), fontSize = 16.sp)
    )

    val concentrationDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("$concentration%"),
        TextStyle(color = Color(18, 95, 202), fontSize = 16.sp),
    )

    Canvas(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .clip(RoundedCornerShape(10.dp))
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height


        drawRoundRect(
            color = color,
            size = Size(
                size.width.dp.toPx(),
                height = (progress * size.height).dp.toPx()
            ),
            topLeft = Offset(0.dp.toPx(), ((1 - progress) * size.height).dp.toPx()),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // background
        drawRoundRect(
            color = backgroundColor,
            size = Size(
                width = size.width.dp.toPx(),
                height = ((1 - progress) * size.height).dp.toPx()
            ),
            cornerRadius = CornerRadius(10f, 10f)
        )

        val textDescWidth = textDescLayoutResult.size.width
        val textDescHeight = textDescLayoutResult.size.height //用不着
        //名称
        drawText(
            textLayoutResult = concentrationDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 1.5f,
                canvasHeight / 2 - 20
            ),
        )
        //液量
        drawText(
            textLayoutResult = volumeDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 1.5f,
                canvasHeight / 2
            ),
        )
        drawText(
            textLayoutResult = textDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2,
                canvasHeight / 2 + 40
            ),
        )


    }
}