package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.zktony.android.data.datastore.DataSaverConverter
import com.zktony.android.data.datastore.DataSaverDataStorePreferences
import com.zktony.android.data.datastore.LocalDataSaver
import com.zktony.android.ext.dsl.axisInitializer
import com.zktony.android.ext.dsl.scheduleTask
import com.zktony.android.ext.dsl.serialPort
import com.zktony.android.ext.dsl.syringeInitializer
import com.zktony.android.ui.theme.AppTheme
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

/**
 * The main activity of the application.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dataSaver =
            DataSaverDataStorePreferences(applicationContext.dataStore)

        DataSaverConverter.registerTypeConverters(
            save = { it.toString() },
            restore = { it.toFloat() }
        )

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
                CompositionLocalProvider(LocalDataSaver provides dataSaver) {
                    App()
                }
            }
        }
    }
}