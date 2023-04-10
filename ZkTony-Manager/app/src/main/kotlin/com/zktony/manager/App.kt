package com.zktony.manager

import android.app.Application
import com.zktony.manager.ext.Ext
import com.zktony.manager.di.localModule
import com.zktony.manager.di.remoteModule
import com.zktony.manager.di.viewModule
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

        startKoin {
            androidContext(this@App)
            androidLogger(level = Level.INFO)
            modules(
                localModule,
                remoteModule,
                viewModule
            )
        }
    }
}