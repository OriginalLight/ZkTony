package com.zktony.android.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

val zktyGreen = Color(0xFF1D9858)
val zktyBlue = Color(0xFF1C68A9)
val zktyYellow = Color(0xFFFF9800)
val zktyBrush = Brush.linearGradient(listOf(zktyGreen, zktyBlue))
val zktyHorizontalBrush = Brush.horizontalGradient(listOf(zktyGreen, zktyBlue))
val zktyChartLinearColors = listOf(zktyGreen, zktyBlue, zktyYellow)

@Preview
@Composable
fun PreviewColorUtils() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(brush = zktyBrush)
        ) {
        }
    }
}

@Preview
@Composable
fun PreviewHorizontalBrush() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(brush = zktyHorizontalBrush)
        ) {
        }
    }
}
