package com.zktony.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * @author 刘贺贺
 * @date 2023/7/25 13:35
 */

@OptIn(ExperimentalTextApi::class)
@Composable
fun OrificePlate(
    modifier: Modifier = Modifier,
    row: Int,
    column: Int,
    selected: List<Pair<Int, Int>> = emptyList(),
    onItemClick: (Int, Int) -> Unit = { _, _ -> }
) {
    check(row > 0) { "row must be greater than 0" }
    check(column > 0) { "column must be greater than 0" }

    val textMeasure = rememberTextMeasurer()

    Box(
        modifier = modifier
            .background(
                color = Color.White,
                shape = CutCornerShape(topStart = 15.dp)
            )
            .shadow(
                elevation = 2.dp,
                shape = CutCornerShape(topStart = 15.dp)
            )
            .padding(4.dp)
    ) {
        // 画一个长度和高度符合view大小的矩形
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        val rowDocker = size.width / (row * 2 + 1f)
                        val columnDocker = size.height / (column * 2 + 1f)
                        val rowSpace = (size.width - rowDocker) / row
                        val columnSpace = (size.height - columnDocker) / column

                        val x = ((it.x - rowDocker) / rowSpace).toInt()
                        val y = ((it.y - columnDocker) / columnSpace).toInt()
                        onItemClick(x, y)
                    }
                }) {

            val rowDocker = size.width / (row * 2 + 1f)
            val columnDocker = size.height / (column * 2 + 1f)

            val rowSpace = (size.width - rowDocker) / row
            val columnSpace = (size.height - columnDocker) / column


            for (i in 0 until column) {
                for (j in 0 until row) {
                    val enable = selected.any { it.first == i && it.second == j }
                    drawCircle(
                        color = if (enable) Color.Green else Color.Black,
                        radius = minOf(columnSpace, rowSpace) / 2.5f,
                        center = Offset(
                            rowDocker + (j + 0.5f) * rowSpace,
                            columnDocker + (i + 0.5f) * columnSpace
                        ),
                        style = if (enable) Fill else
                            Stroke(minOf(columnSpace, rowSpace) / 15f)
                    )
                }
            }

            val textStyle = TextStyle(fontSize = (minOf(columnSpace, rowSpace) / 3f).toSp())

            for (i in 0 until column) {
                val text = "${'A' + i}"
                val textSize = textMeasure.measure(text, textStyle).size
                drawText(
                    textMeasurer = textMeasure,
                    text = text,
                    style = textStyle,
                    topLeft = Offset(
                        rowDocker / 2f - textSize.width / 2f,
                        columnDocker + (i + 0.5f) * columnSpace - textSize.height / 2f
                    ),
                )
            }

            for (i in 0 until row) {
                val text = "${i + 1}"
                val textSize = textMeasure.measure(text, textStyle).size
                drawText(
                    textMeasurer = textMeasure,
                    text = text,
                    style = textStyle,
                    topLeft = Offset(
                        rowDocker + (i + 0.5f) * rowSpace - textSize.width / 2f,
                        columnDocker / 2f - textSize.height / 2f
                    ),
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
        selected = listOf(0 to 0, 0 to 1, 0 to 2, 0 to 3, 0 to 4, 0 to 5, 0 to 6, 0 to 7)
    )
}