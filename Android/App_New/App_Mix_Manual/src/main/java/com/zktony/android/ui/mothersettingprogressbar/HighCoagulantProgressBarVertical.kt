package com.zktony.android.ui.mothersettingprogressbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

/**
 * 低浓度竖形进度条
 * progress 进度
 * color    进度颜色
 * backgroundColor  底色
 */
@Composable
fun HighCoagulantProgressBarVertical(
    progress: MutableState<Float>,
    volume: String,
    concentration: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    backgroundColor: Color = Color.White,
    size: Size = Size(width = 100f, height = 200f),
    strokeSize: Float = 0.1f,
    strokeColor: Color = Color.Blue
) {

    val textDesc = "高浓度"
    val textDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString(textDesc),
        TextStyle(color = Color(178, 193, 209))
    )


    val volumeDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("液量:" + volume),
        TextStyle(color = Color(178, 193, 209))
    )

    val concentrationDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("浓度:" + concentration + "%"),
        TextStyle(color = Color(178, 193, 209))
    )

    Canvas(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .border(width = strokeSize.dp, color = strokeColor,shape = RoundedCornerShape(10.dp))
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height


        drawRoundRect(
            color = color,
            size = Size(
                size.width.dp.toPx(),
                height = (progress.value * size.height).dp.toPx()
            ),
            topLeft = Offset(0.dp.toPx(), ((1 - progress.value) * size.height).dp.toPx()),
            cornerRadius = CornerRadius ( 10f , 10f )
        )
        // background
        drawRoundRect(
            color = backgroundColor,
            size = Size(
                width = size.width.dp.toPx(),
                height = ((1 - progress.value) * size.height).dp.toPx()
            ),
            cornerRadius = CornerRadius ( 10f , 10f )
        )

        val textDescWidth = textDescLayoutResult.size.width
        val textDescHeight = textDescLayoutResult.size.height //用不着
        //名称
        drawText(
            textLayoutResult = textDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2,
                canvasHeight / 2 - 20
            ),
        )
        //液量
        drawText(
            textLayoutResult = volumeDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2 - 10,
                canvasHeight / 2+ 20
            ),
        )
        drawText(
            textLayoutResult = concentrationDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2 - 10,
                canvasHeight / 2
            ),
        )


    }
}