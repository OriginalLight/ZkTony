package com.zktony.android

import android.app.Application
import com.zktony.android.di.localModule
import com.zktony.android.di.proxyModule
import com.zktony.android.di.remoteModule
import com.zktony.android.di.viewModule
import com.zktony.core.ext.Ext
import com.zktony.datastore.DataStoreFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 12:53
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Ext.with(this)
        DataStoreFactory.init(this)

        startKoin {
            androidContext(this@App)
            androidLogger(level = Level.INFO)
            modules(
                localModule,
                remoteModule,
                viewModule,
                proxyModule,
            )
        }
    }
}