package com.zktony.www

import android.app.Application
import com.zktony.common.dialog.DialogXManager
import com.zktony.common.ext.Ext
import com.zktony.www.data.local.datastore.DataStoreFactory
import com.zktony.www.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

/**
 * @author: 刘贺贺
 * @date: 2022-09-14 17:25
 */
class App : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        Ext.with(this)
        DataStoreFactory.init(this)
        DialogXManager(this).init()

        startKoin {
            androidContext(this@App)
            workManagerFactory()
            modules(
                localModule,
                remoteModule,
                workerModule,
                managerModule,
                viewModule
            )
        }
    }
}