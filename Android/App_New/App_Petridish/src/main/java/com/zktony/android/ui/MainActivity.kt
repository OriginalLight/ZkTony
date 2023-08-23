package com.zktony.android.ui

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.zktony.android.data.datastore.LocalDataSaver
import com.zktony.android.ui.theme.AppTheme
import com.zktony.android.utils.ext.dataSaver


val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

/**
 * The main activity of the application.
 */
/**
 * 培养皿
 * 1.举升1是电机1
 * 2.举升2是电机0
 * 3.夹爪是电机2
 * 4.上盘是电机5
 * 5.下盘是电机4
 * 紫外线灯是6
 * 检测是7和8
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            AppTheme {
                CompositionLocalProvider(LocalDataSaver provides dataSaver) {
                    App()
                }
            }
        }
    }
}