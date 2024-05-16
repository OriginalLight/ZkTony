package com.zktony.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

/**
 * @author 刘贺贺
 * @date 2023/7/25 13:35
 */

@Composable
fun OrificePlate(
    modifier: Modifier = Modifier,
    row: Int,
    column: Int,
    coordinate: Boolean = false,
    selected: List<Triple<Int, Int, Color>> = emptyList(),
    onItemClick: (IntSize, Offset) -> Unit = { _, _ -> }
) {
    check(row > 0) { "row must be greater than 0" }
    check(column > 0) { "column must be greater than 0" }

    val textMeasure = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .border(
                width = 1.dp,
                color = Color.LightGray,
                shape = MaterialTheme.shapes.small
            )
            .padding(4.dp)
    ) {
        // 画一个长度和高度符合view大小的矩形
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        onItemClick(size, it)
                    }
                }) {

            val rowSpace = size.width / row
            val columnSpace = size.height / column

            for (i in 0 until column) {
                for (j in 0 until row) {
                    val enable = selected.find { it.first == i && it.second == j }
                    drawCircle(
                        color = enable?.third ?: Color.Black,
                        radius = minOf(columnSpace, rowSpace) / 2.5f,
                        center = Offset(
                            (j + 0.5f) * rowSpace,
                            (i + 0.5f) * columnSpace
                        ),
                        style = if (enable != null) Fill else Stroke(
                            minOf(
                                columnSpace,
                                rowSpace
                            ) / 18f
                        )
                    )
                }
            }

            if (coordinate) {
                val textStyle = TextStyle(
                    color = Color.DarkGray,
                    fontSize = (minOf(columnSpace, rowSpace) / 3f).toSp()
                )
                val text1 = "A1"
                val text2 = "${'A' + column - 1}${row}"
                val textSize1 = textMeasure.measure(text1, textStyle).size
                val textSize2 = textMeasure.measure(text2, textStyle).size
                drawText(
                    textMeasurer = textMeasure,
                    text = text1,
                    style = textStyle,
                    topLeft = Offset(
                        x = (rowSpace - textSize1.width) / 2,
                        y = (columnSpace - textSize1.height) / 2
                    )
                )
                drawText(
                    textMeasurer = textMeasure,
                    text = text2,
                    style = textStyle,
                    topLeft = Offset(
                        x = size.width - (rowSpace - textSize2.width) / 2 - textSize2.width,
                        y = size.height - (columnSpace - textSize2.height) / 2 - textSize2.height
                    )
                )
            }
        }
    }
}


@Preview
@Composable
fun OrificePlatePreview() {
    OrificePlate(
        modifier = Modifier
            .width(400.dp)
            .height(250.dp),
        row = 12,
        column = 8,
        coordinate = true,
    )
}