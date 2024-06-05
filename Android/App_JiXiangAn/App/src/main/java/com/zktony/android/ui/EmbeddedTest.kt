package com.zktony.android.ui

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zktony.android.utils.extra.UpgradeState
import com.zktony.android.utils.extra.embeddedUpgrade
import com.zktony.android.utils.extra.embeddedVersion
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun EmbeddedTest() {

    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("下位机升级") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = text, style = MaterialTheme.typography.displaySmall)
            Button(onClick = {
                scope.launch {
                    val ver = embeddedVersion()
                    Log.e("EmbeddedTest", "version: $ver")
                    val media = "/mnt/media_rw/AC7D-16F8"
                    val hex = File(media, "test.bin")
                    if (!hex.exists()) {
                        text = "文件不存在"
                        return@launch
                    }
                    embeddedUpgrade(hex).collect {
                        text = when(it) {
                            is UpgradeState.Success -> "升级成功"
                            is UpgradeState.Err -> "${it.t.message}"
                            is UpgradeState.Progress -> "升级中 ${it.progress * 100} %"
                        }
                    }
                }
            }) {
                Text(text = "开始升级")
            }
        }
    }
}

