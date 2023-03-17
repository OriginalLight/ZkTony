package com.zktony.www.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zktony.www.data.local.datastore.DataStoreFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


/**
 * [DataStore] 提供者
 */
@InstallIn(SingletonComponent::class)
@Module
object DataStoreModule {

    @Singleton
    @Provides
    fun provideDefaultDataStore(): DataStore<Preferences> =
        DataStoreFactory.getDefaultPreferencesDataStore()
}