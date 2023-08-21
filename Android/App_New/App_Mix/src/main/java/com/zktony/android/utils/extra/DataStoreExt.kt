package com.zktony.android.utils.ext

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.zktony.android.data.datastore.DataSaverDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("dataStore")

val dataSaver = DataSaverDataStore(Ext.ctx.dataStore)