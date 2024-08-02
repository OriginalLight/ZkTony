package com.zktony.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.ui.utils.zktyGreen
import com.zktony.android.ui.utils.zktyYellow
import com.zktony.android.utils.SnackbarUtils
import com.zktony.android.utils.TipsUtils

enum class TipsType {
    INFO,
    WARNING,
    ERROR
}

data class Tips(
    val type: TipsType,
    val message: String
) {
    companion object {
        fun info(message: String) = Tips(TipsType.INFO, message)
        fun warning(message: String) = Tips(TipsType.WARNING, message)
        fun error(message: String) = Tips(TipsType.ERROR, message)
    }
}

@Composable
fun Tips(modifier: Modifier = Modifier) {
    val tips by TipsUtils.tips.collectAsStateWithLifecycle()

    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .clickable { tips?.let { SnackbarUtils.showSnackbar(it.message) } }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TipsIcon(tips)
        TipsMessage(tips)
    }
}

@Composable
fun TipsIcon(tips: Tips?) {
    when (tips?.type) {
        TipsType.INFO -> {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        TipsType.WARNING -> {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = zktyYellow
            )
        }

        TipsType.ERROR -> {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error
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
        TipsUtils.showTips(Tips.info("这个是一个提示信息"))
        Tips()
    }
}