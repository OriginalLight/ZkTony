package com.zktony.www.di

import androidx.room.Room
import com.zktony.datastore.DataStoreFactory
import com.zktony.www.data.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val localModule = module {
    single { DataStoreFactory.getDefaultPreferencesDataStore() }
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "zktony.db"
        ).build()
    }
    single { get<AppDatabase>().programDao() }
    single { get<AppDatabase>().logRecordDao() }
    single { get<AppDatabase>().logDataDao() }
}