package com.zktony.www

import android.app.Application
import android.content.Context
import com.zktony.common.dialog.DialogXManager
import com.zktony.common.ext.Ext
import com.zktony.www.data.local.datastore.DataStoreFactory
import com.zktony.www.di.localModule
import com.zktony.www.di.remoteModule
import com.zktony.www.di.viewModule
import com.zktony.www.di.workerModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin

/**
 * @author: 刘贺贺
 * @date: 2022-09-14 17:25
 */
class App : Application(), KoinComponent {

    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        appContext = applicationContext
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
                viewModule,
                workerModule
            )
        }
    }

}