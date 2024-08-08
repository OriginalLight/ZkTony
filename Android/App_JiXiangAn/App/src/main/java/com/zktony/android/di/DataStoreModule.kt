package com.zktony.android.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zktony.android.data.datastore.DataSaverDataStore
import com.zktony.android.utils.extra.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    fun dataStore(@ApplicationContext appContext: Context): DataStore<Preferences> {
        return appContext.dataStore
    }

    @Provides
    fun dataSaverDataStore(dataStore: DataStore<Preferences>) = DataSaverDataStore(dataStore)
}