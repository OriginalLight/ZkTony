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
 * 培养皿
 * 举升1是电机1
 * 举升2是电机0
 * 夹爪是电机2
 * 上盘是电机3
 * 下盘是电机4
 * 紫外线灯是电磁阀5
 * 蠕动泵1是5
 * 蠕动泵2是6
 * 检测培养皿是否摆放正确（光电）7
 * 检测是否有培养皿（光电）6
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