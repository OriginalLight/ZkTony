package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.zktony.android.core.dsl.axisInitializer
import com.zktony.android.core.dsl.scheduleTask
import com.zktony.android.core.dsl.serialPort
import com.zktony.android.core.dsl.syringeInitializer
import com.zktony.android.core.ext.setLanguage
import com.zktony.android.ui.theme.AppTheme
import com.zktony.datastore.ext.settings
import kotlinx.coroutines.launch

/**
 * The main activity of the application.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the serial port, schedule task, and syringe and axis positions
        lifecycleScope.launch {
            serialPort.initializer() // Step 1: Initialize the serial port
            scheduleTask.initializer() // Step 2: Initialize the schedule task
            axisInitializer(1, 0) // Step 3: Initialize the axis
            syringeInitializer(2) // Step 4: Initialize the syringe
        }

        // Set the content view of the activity
        setContent {
            AppTheme {
                App()
            }
        }
    }

    /**
     * Attaches the specified context with the specified language to the base context of the activity.
     *
     * @param newBase The new base context to attach.
     */
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.setLanguage(settings.language)
        )
    }
}