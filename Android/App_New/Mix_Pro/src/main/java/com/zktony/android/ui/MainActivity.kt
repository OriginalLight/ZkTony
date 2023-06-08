package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zktony.android.logic.ext.scheduleTask
import com.zktony.android.logic.ext.serialPort
import com.zktony.android.ui.theme.AppTheme
import com.zktony.core.ext.setLanguage
import com.zktony.datastore.ext.settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serialPort.initializer()
        scheduleTask.initializer()

        setContent {
            AppTheme(
                dynamicColor = false
            ) {
                ZktyApp()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.setLanguage(settings.language)
        )
    }
}