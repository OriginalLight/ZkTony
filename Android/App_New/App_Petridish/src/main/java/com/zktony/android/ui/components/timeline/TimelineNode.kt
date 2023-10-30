package com.zktony.android.ui.components.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zktony.android.ui.components.timeline.defaults.CircleParametersDefaults
import com.zktony.android.ui.components.timeline.defaults.LineParametersDefaults

@Composable
fun TimelineNode(
    position: TimelineNodePosition,
    circleParameters: CircleParameters,
    lineParameters: LineParameters? = null,
    contentStartOffset: Dp = 16.dp,
    spacer: Dp = 32.dp,
    content: @Composable BoxScope.(modifier: Modifier) -> Unit
) {
    val iconPainter = circleParameters.icon?.let { rememberVectorPainter(image = it) }
    Box(
        modifier = Modifier
            .wrapContentSize()
            .drawBehind {
                val circleRadiusInPx = circleParameters.radius.toPx()

                lineParameters?.let {
                    drawLine(
                        brush = it.brush,
                        start = Offset(x = circleRadiusInPx, y = circleRadiusInPx * 2),
                        end = Offset(x = circleRadiusInPx, y = this.size.height),
                        strokeWidth = it.strokeWidth.toPx()
                    )
                }

                drawCircle(
                    circleParameters.backgroundColor,
                    circleRadiusInPx,
                    center = Offset(x = circleRadiusInPx, y = circleRadiusInPx)
                )

                circleParameters.stroke?.let { stroke ->
                    val strokeWidthInPx = stroke.width.toPx()
                    drawCircle(
                        color = stroke.color,
                        radius = circleRadiusInPx - strokeWidthInPx / 2,
                        center = Offset(x = circleRadiusInPx, y = circleRadiusInPx),
                        style = Stroke(width = strokeWidthInPx)
                    )
                }

                iconPainter?.let { painter ->
                    this.withTransform(
                        transformBlock = {
                            translate(
                                left = circleRadiusInPx - painter.intrinsicSize.width / 3f,
                                top = circleRadiusInPx - painter.intrinsicSize.height / 3f
                            )
                        },
                        drawBlock = {
                            this.drawIntoCanvas {
                                with(painter) {
                                    draw(
                                        size = intrinsicSize / 1.5f,
                                        colorFilter = circleParameters.iconColorFilter
                                    )
                                }
                            }
                        })
                }
            }
    ) {
        content(
            Modifier
                .defaultMinSize(minHeight = circleParameters.radius * 2)
                .padding(
                    start = circleParameters.radius * 2 + contentStartOffset,
                    bottom = if (position != TimelineNodePosition.LAST) spacer else 0.dp
                )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimelinePreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TimelineNode(
            position = TimelineNodePosition.FIRST,
            circleParameters = CircleParametersDefaults.circleParameters(
                backgroundColor = Color.Blue,
                icon = Icons.Default.Home
            ),
            lineParameters = LineParametersDefaults.linearGradient(
                startColor = Color.Blue,
                endColor = Color.Green,
            )
        ) { modifier -> MessageBubble(modifier, containerColor = Color.Blue) }

        TimelineNode(
            position = TimelineNodePosition.MIDDLE,
            circleParameters = CircleParametersDefaults.circleParameters(
                backgroundColor = Color.Cyan
            ),
            contentStartOffset = 16.dp,
            lineParameters = LineParametersDefaults.linearGradient(
                startColor = Color.Cyan,
                endColor = Color.Cyan
            )
        ) { modifier -> MessageBubble(modifier, containerColor = Color.Cyan) }

    }
}

@Composable
private fun MessageBubble(modifier: Modifier, containerColor: Color) {
    Card(
        modifier = modifier
            .width(200.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Text(text = "Hello World")
    }
}
