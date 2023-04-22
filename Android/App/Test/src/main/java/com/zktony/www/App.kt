package com.zktony.www

import android.app.Application
import com.zktony.core.ext.Ext
import com.zktony.core.ext.initDialogX
import com.zktony.core.ext.initTypeface
import com.zktony.datastore.DataStoreFactory
import com.zktony.www.di.localModule
import com.zktony.www.di.viewModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin


class App : Application(), KoinComponent {


    override fun onCreate() {
        super.onCreate()
        Ext.with(this)
        this.initTypeface()
        DataStoreFactory.init(this)
        initDialogX(this)

        startKoin {
            androidContext(this@App)
            modules(
                localModule,
                viewModule
            )
        }
    }
}