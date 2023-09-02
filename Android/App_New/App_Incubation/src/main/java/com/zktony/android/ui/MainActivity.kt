package com.zktony.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.utils.service.setupServices

/**
 * The main activity of the application.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupServices()

        setContent {
            AppTheme { App() }
        }
    }
}