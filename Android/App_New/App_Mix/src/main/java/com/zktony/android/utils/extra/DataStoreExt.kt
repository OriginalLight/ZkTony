package com.zktony.android.utils.extra

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.zktony.android.data.datastore.DataSaverDataStore

/**
 * @author 刘贺贺
 * @date 2023/8/18 9:20
 */

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

val dataSaver = DataSaverDataStore(Ext.ctx.dataStore)