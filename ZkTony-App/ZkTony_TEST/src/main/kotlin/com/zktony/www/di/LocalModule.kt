package com.zktony.www.di

import androidx.room.Room
import com.zktony.common.ext.Ext
import com.zktony.www.common.datastore.DataStoreFactory
import com.zktony.www.common.room.database.AppDatabase
import org.koin.dsl.module

val localModule = module {
    single { DataStoreFactory.getDefaultPreferencesDataStore() }
    single {
        Room.databaseBuilder(
            Ext.ctx,
            AppDatabase::class.java,
            "zktony.db"
        ).build()
    }
    single { get<AppDatabase>().testDao() }
}