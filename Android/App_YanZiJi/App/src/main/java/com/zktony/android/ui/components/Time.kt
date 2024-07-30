package com.zktony.android.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

// 实时显示时间
@Composable
fun Time() {
    var currentDateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(key1 = true) {
        // 获取当前时间的秒数
        val currentMillis = System.currentTimeMillis()
        val currentSeconds = TimeUnit.MILLISECONDS.toSeconds(currentMillis) % 60

        // 计算到下一个整分钟的时间差
        val delayMillis = (60 - currentSeconds) * 1000L
        delay(delayMillis)

        // 进入无限循环，每分钟执行一次
        while (true) {
            currentDateTime = System.currentTimeMillis()
            delay(60 * 1000)
        }
    }

    Text(
        text = currentDateTime.dateFormat("HH:mm\nyyyy-MM-dd"),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.End
    )
}

@Preview
@Composable
fun TimePreview() {
    Surface {
        Time()
    }
}