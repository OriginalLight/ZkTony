package com.zktony.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.data.ExperimentalState

@Composable
fun ExperimentalState(
    modifier: Modifier = Modifier,
    state: ExperimentalState
) {
    // 画一个圆16.dp
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(
                color = Color.White,
                radius = 12f
            )
            drawCircle(
                color = state.color,
                radius = 10f
            )
        }

        Text(
            text = state.text,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

@Preview
@Composable
fun ExperimentalStatePreview() {
    ExperimentalState(state = ExperimentalState.NONE)
}