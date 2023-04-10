package com.zktony.www.di

import androidx.room.Room
import com.zktony.datastore.DataStoreFactory
import com.zktony.common.ext.Ext
import com.zktony.www.room.database.AppDatabase
import org.koin.dsl.module

val localModule = module {
    single {  DataStoreFactory.getDefaultPreferencesDataStore() }
    single {
        Room.databaseBuilder(
            Ext.ctx,
            AppDatabase::class.java,
            "zktony.db"
        ).build()
    }
    single { get<AppDatabase>().logDao() }
    single { get<AppDatabase>().motorDao() }
    single { get<AppDatabase>().calibrationDao() }
    single { get<AppDatabase>().calibrationDataDao() }
    single { get<AppDatabase>().containerDao() }
    single { get<AppDatabase>().actionDao() }
    single { get<AppDatabase>().programDao() }
}