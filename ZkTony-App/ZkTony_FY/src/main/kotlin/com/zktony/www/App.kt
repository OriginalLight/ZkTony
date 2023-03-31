package com.zktony.www

import android.app.Application
import com.zktony.common.datastore.DataStoreFactory
import com.zktony.common.dialog.DialogXManager
import com.zktony.common.ext.Ext
import com.zktony.www.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

class App : Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        Ext.with(this)
        DataStoreFactory.init(this)
        DialogXManager.init(this)

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