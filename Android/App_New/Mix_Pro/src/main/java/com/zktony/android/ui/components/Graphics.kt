package com.zktony.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * @author 刘贺贺
 * @date 2023/5/11 13:19
 */

@Composable
fun DynamicMixPlate(
    modifier: Modifier = Modifier,
    count: Int,
    data: List<Pair<Int, Boolean>>,
    onItemClick: (Int) -> Unit = {}
) {

    // 画一个长度和高度符合view大小的矩形

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        val space = size.width / count
                        onItemClick((it.x / space).toInt())
                    }
                )
            }
    ) {
        val space = size.width / count

        drawRect(
            color = Color.Black,
            topLeft = Offset(0f, 0f),
            size = size,
            style = Stroke(4f)
        )

        // 画count个圆形
        for (i in 0 until count) {
            drawCircle(
                color = Color.Black,
                radius = space / 2.5f,
                center = Offset((i + 0.5f) * space, size.height / 2f),
                style = Stroke(4f)
            )
        }

        // 画count个圆形
        for (i in 0 until count) {
            val fill = data.getOrNull(i)?.second ?: false
            if (fill) {
                drawCircle(
                    color = Color.Green,
                    radius = space / 2.5f,
                    center = Offset((i + 0.5f) * space, size.height / 2f),
                )
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun DynamicMixPlatePreview() {
    Column(modifier = Modifier.padding(16.dp)) {
        DynamicMixPlate(count = 6, modifier = Modifier, data = listOf(Pair(0, true)))
    }
}
