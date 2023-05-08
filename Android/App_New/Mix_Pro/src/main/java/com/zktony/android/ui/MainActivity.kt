package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zktony.android.core.ext.proxyInitializer
import com.zktony.android.ui.theme.AppTheme
import com.zktony.core.ext.setLanguage
import com.zktony.core.utils.Constants
import com.zktony.datastore.ext.read
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val datastore: DataStore<Preferences> by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        proxyInitializer()

        setContent {
            AppTheme {
                ZkTonyApp()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(
            newBase?.setLanguage(runBlocking { datastore.read(Constants.LANGUAGE, "zh").first() })
        )
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 640)
@Composable
fun AppPreview() {
    AppTheme {
        ZkTonyApp()
    }
}
