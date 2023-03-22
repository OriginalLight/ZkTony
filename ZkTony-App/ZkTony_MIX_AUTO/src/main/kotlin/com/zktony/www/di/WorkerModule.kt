package com.zktony.www.di

import com.zktony.common.ext.Ext
import com.zktony.www.common.worker.LogWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    worker { LogWorker(get(), Ext.ctx, get()) }
}