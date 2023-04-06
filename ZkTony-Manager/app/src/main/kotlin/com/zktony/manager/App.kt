package com.zktony.manager

import android.app.Application
import com.zktony.manager.common.ext.Ext
import com.zktony.manager.di.localModule
import com.zktony.manager.di.remoteModule
import com.zktony.manager.di.viewModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

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
            modules(
                localModule,
                remoteModule,
                viewModule
            )
        }
    }
}