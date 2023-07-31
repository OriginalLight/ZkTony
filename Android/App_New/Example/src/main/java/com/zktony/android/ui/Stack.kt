package com.zktony.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.zktony.android.ui.components.OrificePlate
import com.zktony.android.utils.ext.loge
import kotlin.math.roundToInt

/**
 * @author 刘贺贺
 * @date 2023/7/26 13:35
 */

@Composable
fun Stack(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.inverseOnSurface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        var graphicsState by remember { mutableStateOf(listOf(GraphicsState(), GraphicsState())) }

        for (i in graphicsState.indices) {
            OrificePlate(
                modifier = Modifier
                    .width(400.dp)
                    .height(250.dp)
                    .offset {
                        IntOffset(
                            graphicsState[i].offset.x.roundToInt(),
                            graphicsState[i].offset.y.roundToInt()
                        )
                    }
                    .scale(graphicsState[i].scale)
                    .pointerInput(Unit) {
                        detectTransformGestures(
                            panZoomLock = true,
                            onGesture = { _, pan, zoom, _ ->
                                val state = graphicsState[i].copy(
                                    scale = graphicsState[i].scale * zoom,
                                    offset = graphicsState[i].offset + pan
                                )
                                graphicsState =
                                    graphicsState
                                        .toMutableList()
                                        .apply { set(i, state) }
                            }
                        )
                    },
                row = 12,
                column = 8,
                selected = listOf(
                    0 to 0,
                    0 to 1,
                    0 to 2,
                    0 to 3,
                    0 to 4,
                    0 to 5,
                    0 to 6,
                    0 to 7
                ),
                onItemClick = { x, y -> "$x,$y".loge() }
            )
        }

    }
}

data class GraphicsState(
    val scale: Float = 1f,
    val offset: Offset = Offset.Zero,
)