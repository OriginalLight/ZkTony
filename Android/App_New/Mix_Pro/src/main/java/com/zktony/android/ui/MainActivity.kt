package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.zktony.android.core.dsl.axisInitializer
import com.zktony.android.core.dsl.scheduleTask
import com.zktony.android.core.dsl.serialPort
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.core.ext.setLanguage
import com.zktony.datastore.ext.settings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            serialPort.initializer()
            scheduleTask.initializer()
            axisInitializer(1, 0)
        }

        setContent {
            AppTheme {
                App()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.setLanguage(settings.language)
        )
    }
}