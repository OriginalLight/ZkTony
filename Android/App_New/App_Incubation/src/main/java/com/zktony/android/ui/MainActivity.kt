package com.zktony.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.zktony.android.data.datastore.LocalDataSaver
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.utils.extra.dataSaver

/**
 * The main activity of the application.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaver) {
                    Test()
                }
            }
        }
    }
}