package com.zktony.www.di

import com.zktony.www.common.worker.LogWorker
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    worker { LogWorker(get(), androidContext(), get()) }
}