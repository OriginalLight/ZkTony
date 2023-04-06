package com.zktony.manager.di

import androidx.room.Room
import com.zktony.manager.common.ext.Ext
import com.zktony.manager.data.local.room.AppDatabase
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