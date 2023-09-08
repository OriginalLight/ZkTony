package com.zktony.www

import android.app.Application
import com.zktony.core.ext.Ext
import com.zktony.core.ext.initDialogX
import com.zktony.core.ext.initTypeface
import com.zktony.core.utils.DataStoreFactory
import com.zktony.www.di.coreModule
import com.zktony.www.di.localModule
import com.zktony.www.di.viewModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * @author: 刘贺贺
 * @date: 2022-09-14 17:25
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Ext.with(this)
        this.initTypeface()
        DataStoreFactory.init(this)
        initDialogX(this)

        startKoin {
            androidContext(this@App)
            androidLogger(Level.INFO)
            modules(
                localModule,
                coreModule,
                viewModule
            )
        }
    }
}