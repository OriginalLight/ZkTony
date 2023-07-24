package com.zktony.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zktony.android.ext.ext.serialPort
import com.zktony.android.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serialPort.initializer()

        setContent {
            AppTheme(
                dynamicColor = false
            ) {
                ZkTonyApp()
            }
        }
    }
}