package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
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
        Box(
            modifier = Modifier
                .size(24.dp)
                .drawWithCache {
                    onDrawWithContent {
                        // 画一个白色的圆
                        drawCircle(
                            color = Color.White,
                            radius = size.width / 2
                        )
                        // 画一个颜色为state.color的圆
                        drawCircle(
                            color = state.color,
                            radius = size.width / 2 - 2.dp.toPx()
                        )
                    }
                }
        ) {}

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