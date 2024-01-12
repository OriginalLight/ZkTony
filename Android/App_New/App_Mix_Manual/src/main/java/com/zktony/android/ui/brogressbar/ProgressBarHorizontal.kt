package com.zktony.android.ui.brogressbar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 横向进度条
 */
@Composable
fun HorizontalProgressBar() {
    var progress by remember {
        mutableStateOf(0.1f)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    )

    Column {
        LinearProgressIndicator(
            modifier = Modifier
                .width(50.dp)
                .height(100.dp),
            progress = animatedProgress
        )
        Spacer(modifier = Modifier.requiredHeight(30.dp))
        OutlinedButton(onClick = { if (progress < 1f) progress += 0.1f }) {
            Text(text = "增加进度")
        }
    }
}