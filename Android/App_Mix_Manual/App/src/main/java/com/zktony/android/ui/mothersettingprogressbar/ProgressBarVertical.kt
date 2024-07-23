package com.zktony.android.ui.mothersettingprogressbar

import android.util.Log
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    color: Color = Color(android.graphics.Color.rgb(136, 196, 254)),
    backgroundColor: Color = Color(229, 229, 229),
    size: Size = Size(width = 401.9f, height = 227.7f),
    strokeSize: Float = 0.1f,
    strokeColor: Color = Color.Blue
) {

    val index = progress.indexOf('.')
    var progress = progress.substring(0, index)


//    val waterDescLayoutResult = rememberTextMeasurer().measure(
//        AnnotatedString("$progress%"),
//        TextStyle(color = Color(18, 95, 202))
//    )

    val zhiDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("制"),
        TextStyle(color = Color(18, 95, 202), fontSize = 20.sp)
    )

    val jiaoDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("胶"),
        TextStyle(color = Color(18, 95, 202), fontSize = 20.sp)
    )

    val jiaDescLayoutResult = rememberTextMeasurer().measure(
        AnnotatedString("架"),
        TextStyle(color = Color(18, 95, 202), fontSize = 20.sp)
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
                height = (waterProgress * size.height).dp.toPx()
            ),
            topLeft = Offset(0.dp.toPx(), ((1 - waterProgress) * size.height).dp.toPx()),
            cornerRadius = CornerRadius(if (waterProgress < 1) 0f else 10f, 10f)
        )
        // background
        drawRoundRect(
            color = backgroundColor,
            size = Size(
                width = size.width.dp.toPx(),
                height = ((1 - waterProgress) * size.height).dp.toPx()
            ),
            cornerRadius = CornerRadius(10f, if (waterProgress < 1) 0f else 10f)
        )

        val textDescWidth = zhiDescLayoutResult.size.width

        drawText(
            textLayoutResult = zhiDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2,
                canvasHeight / 2 - 20
            ),
        )
        drawText(
            textLayoutResult = jiaoDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2,
                canvasHeight / 2
            ),
        )
        drawText(
            textLayoutResult = jiaDescLayoutResult,
            topLeft = Offset(
                canvasWidth / 2 - textDescWidth / 2,
                canvasHeight / 2 + 20
            ),
        )

    }
}