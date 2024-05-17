package com.zktony.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CircularButtons(
    count: Int = 12,
    display: List<String>,
    radius: Dp = 24.dp,
    enabled: Boolean = true,
    circleRadius: Dp = 300.dp,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    val angleStep = (2 * PI / count).toFloat()

    Box(
        modifier = Modifier.size(circleRadius),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .size(circleRadius)
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                )
                .clip(CircleShape)
        ) {}

        // 绘制圆形按钮
        for (i in 0 until count) {
            val angle = i * angleStep
            val x = (circleRadius / 2).value * cos(angle)
            val y = (circleRadius / 2).value * sin(angle)
            val isSelected = selected == i

            Button(
                onClick = { onSelected(i) },
                enabled = enabled,
                modifier = Modifier
                    .size(radius * 2)
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .background(
                        color = if (isSelected) Color.Red else Color.Green,
                        shape = CircleShape
                    )
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable { onSelected(i) }
            ) {}
        }

        Text(
            text = display[selected],
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview
@Composable
fun CircularButtonsWithSelectionPreview() {
    var selected by remember { mutableIntStateOf(0) }

    CircularButtons(
        selected = selected,
        display = listOf("1", "2", "3", "4", "5", "6"),
        onSelected = { index ->
            selected = index
        }
    )
}
