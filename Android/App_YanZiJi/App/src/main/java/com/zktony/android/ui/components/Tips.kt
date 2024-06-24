package com.zktony.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.utils.TipsUtils

enum class TipsType {
    INFO,
    WARNING,
    ERROR
}

data class Tips(
    val type: TipsType,
    val message: String
)

@Composable
fun Tips() {
    val tips = TipsUtils.tips.collectAsStateWithLifecycle()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipsIcon(tips.value)
        TipsMessage(tips.value)
    }
}

@Composable
fun TipsIcon(tips: Tips?) {
    when (tips?.type) {
        TipsType.INFO -> {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = Color.Green
            )
        }

        TipsType.WARNING -> {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color.Yellow
            )
        }

        TipsType.ERROR -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = Color.Red
            )
        }

        null -> {
            // Do nothing
        }
    }
}

@Composable
fun TipsMessage(tips: Tips?) {
    tips?.message?.let {
        Text(text = it, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview
@Composable
fun TipsPreview() {
    Surface {
        TipsUtils.showTips(Tips(TipsType.INFO, "这个是一个提示信息"))
        Tips()
    }
}