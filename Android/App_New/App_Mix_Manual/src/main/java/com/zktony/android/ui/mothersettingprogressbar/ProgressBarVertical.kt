package com.zktony.android.ui.mothersettingprogressbar

import android.util.Log
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
import com.zktony.android.utils.SerialPortUtils

/**
 * 制胶进度竖形进度条
 * progress 进度
 * color    进度颜色
 * backgroundColor  底色
 */
@Composable
fun VerticalProgressBar(
    waterProgress: Float,
    progress: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Green,
    backgroundColor: Color = Color.White,
    size: Size = Size(width = 400f, height = 200f),
    strokeSize: Float = 0.1f,
    strokeColor: Color = Color.Blue
) {

    val index = progress.indexOf('.')
    var progress = progress.substring(0, index)



    val waterDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("$progress%"),
        TextStyle(color = Color(178, 193, 209))
    )

    Canvas(
        modifier = modifier
            .size(size.width.dp, size.height.dp)
            .border(width = strokeSize.dp, color = strokeColor, shape = RoundedCornerShape(10.dp))
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height


        drawRoundRect(
            color = color,
            size = Size(
                size.width.dp.toPx(),
                height = (waterProgress * size.height).dp.toPx()
            ),
            topLeft = Offset(0.dp.toPx(), ((1 - waterProgress) * size.height).dp.toPx()),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // background
        drawRoundRect(
            color = backgroundColor,
            size = Size(
                width = size.width.dp.toPx(),
                height = ((1 - waterProgress) * size.height).dp.toPx()
            ),
            cornerRadius = CornerRadius(10f, 10f)
        )

        val textDescWidth = waterDescLayoutResult.size.width
        //液量
        drawText(
            textLayoutResult = waterDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2 - 10,
                canvasHeight / 2
            ),
        )

    }
}