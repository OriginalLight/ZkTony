package com.zktony.www

import android.app.Application
import com.zktony.core.ext.*
import com.zktony.datastore.DataStoreFactory
import com.zktony.www.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class App : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        Ext.with(this)
        this.initTypeface()
        DataStoreFactory.init(this)
        initDialogX(this)

        startKoin {
            androidContext(this@App)
            androidLogger(Level.INFO)
            workManagerFactory()
            modules(
                localModule,
                remoteModule,
                managerModule,
                viewModule
            )
        }
    }

}