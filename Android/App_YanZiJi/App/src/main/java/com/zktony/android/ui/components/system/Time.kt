package com.zktony.android.ui.components.system

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.zktony.android.utils.extra.dateFormat
import kotlinx.coroutines.delay
import java.util.Date

// 实时显示时间
@Composable
fun Time() {
    val currentDateTime = remember { mutableStateOf(Date(System.currentTimeMillis())) }

    LaunchedEffect(key1 = true) {
        while (true) {
            currentDateTime.value = Date(System.currentTimeMillis())
            delay(1000)
        }
    }

    Text(
        text = currentDateTime.value.dateFormat("yyyy-MM-dd HH:mm:ss"),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview
@Composable
fun TimePreview() {
    Surface {
        Time()
    }
}