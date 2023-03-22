package com.zktony.www.di

import com.zktony.common.ext.Ext
import com.zktony.www.common.worker.LogDataWorker
import com.zktony.www.common.worker.LogRecordWorker
import com.zktony.www.common.worker.LogWorker
import com.zktony.www.common.worker.ProgramWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val workerModule = module {
    worker { LogWorker(get(), get(), Ext.ctx, get()) }
    worker { LogDataWorker(get(), get(), Ext.ctx, get()) }
    worker { LogRecordWorker(get(), get(), Ext.ctx, get()) }
    worker { ProgramWorker(get(), get(), Ext.ctx, get()) }
}