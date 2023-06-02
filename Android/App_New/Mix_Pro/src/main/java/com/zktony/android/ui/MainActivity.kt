package com.zktony.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zktony.android.logic.ext.scheduleTask
import com.zktony.android.logic.ext.serialPort
import com.zktony.android.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scheduleTask.initializer()
        serialPort.initializer()

        setContent {
            AppTheme(
                dynamicColor = false
            ) {
                ZktyApp()
            }
        }
    }
}