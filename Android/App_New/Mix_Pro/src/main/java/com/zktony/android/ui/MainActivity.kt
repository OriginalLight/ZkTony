package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.zktony.android.core.ext.scheduleTask
import com.zktony.android.core.ext.serialPort
import com.zktony.android.ui.theme.AppTheme
import com.zktony.core.ext.setLanguage
import com.zktony.datastore.ext.settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleTask.initializer()
        serialPort.initializer()

        setContent {
            AppTheme {
                ZkTonyApp()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.setLanguage(settings.language)
        )
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 640)
@Composable
fun AppPreview() {
    AppTheme {
        ZkTonyApp()
    }
}