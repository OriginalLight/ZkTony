package com.zktony.manager.di

import androidx.room.Room
import com.zktony.manager.ext.Ext
import com.zktony.manager.data.local.database.AppDatabase
import org.koin.dsl.module

val localModule = module {
    single {
        Room.databaseBuilder(
            Ext.ctx,
            AppDatabase::class.java,
            "data.db"
        ).build()
    }
    single { get<AppDatabase>().userDao() }
}