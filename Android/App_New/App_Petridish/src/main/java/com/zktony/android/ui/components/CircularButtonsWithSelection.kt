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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun CircularButtonsWithSelection(
    buttonCount: Int = 8,
    buttonRadius: Dp = 24.dp,
    buttonEnabled: Boolean = true,
    circleRadius: Dp = 300.dp,
    selectedButtonIndex: Int,
    onButtonSelected: (Int) -> Unit
) {
    val angleStep = (2 * PI / buttonCount).toFloat()

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
        for (i in 0 until buttonCount) {
            val angle = i * angleStep
            val x = (circleRadius / 2).value * cos(angle)
            val y = (circleRadius / 2).value * sin(angle)
            val isSelected = selectedButtonIndex == i

            Button(
                onClick = { onButtonSelected(i) },
                enabled = buttonEnabled,
                modifier = Modifier
                    .size(buttonRadius * 2)
                    .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .background(
                        color = if (isSelected) Color.Red else Color.Green,
                        shape = CircleShape
                    )
                    .padding(8.dp)
                    .clip(CircleShape)
                    .clickable { onButtonSelected(i) }
            ) {}
        }

        Text(
            text = (selectedButtonIndex + 1).toString(),
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Preview
@Composable
fun CircularButtonsWithSelectionPreview() {
    var selectedButton by remember { mutableIntStateOf(0) }

    CircularButtonsWithSelection(
        selectedButtonIndex = selectedButton,
        onButtonSelected = { index ->
            selectedButton = index
        }
    )
}
