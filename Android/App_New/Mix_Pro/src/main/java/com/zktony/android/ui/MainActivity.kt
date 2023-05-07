package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zktony.android.ext.proxyInitializer
import com.zktony.android.ui.theme.ManagerTheme
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
            ManagerTheme(
                dynamicColor = false,
            ) {
                ManagerApp()
            }
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        var language: String
        runBlocking {
            language = datastore.read(Constants.LANGUAGE, "zh").first()
        }
        super.attachBaseContext(
            newBase?.setLanguage(language)
        )
    }
}

@Preview(showBackground = true, widthDp = 960, heightDp = 640)
@Composable
fun AppPreview() {
    ManagerTheme {
        ManagerApp()
    }
}
