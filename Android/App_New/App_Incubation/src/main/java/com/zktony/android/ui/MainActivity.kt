package com.zktony.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import com.zktony.android.data.datastore.LocalDataSaver
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.utils.extra.dataSaver
import com.zktony.android.utils.extra.internal.CurveFactory

/**
 * The main activity of the application.
 */
class MainActivity : ComponentActivity() {

    private val curveFactory = CurveFactory()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        curveFactory.setup()

        setContent {
            AppTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaver) {
                    App()
                }
            }
        }
    }
}