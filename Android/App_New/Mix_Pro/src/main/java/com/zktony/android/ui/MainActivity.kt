package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.zktony.android.logic.ext.axisInitializer
import com.zktony.android.logic.ext.scheduleTask
import com.zktony.android.logic.ext.serialPort
import com.zktony.android.ui.theme.AppTheme
import com.zktony.core.ext.setLanguage
import com.zktony.datastore.ext.settings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            serialPort.initializer()
            scheduleTask.initializer()
            axisInitializer()
        }
        
        setContent {
            AppTheme {
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