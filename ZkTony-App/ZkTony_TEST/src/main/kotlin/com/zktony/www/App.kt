package com.zktony.www

import android.app.Application
import com.zktony.common.dialog.DialogXManager
import com.zktony.common.ext.Ext
import com.zktony.www.common.datastore.DataStoreFactory
import com.zktony.www.di.localModule
import com.zktony.www.di.viewModule
import org.koin.android.ext.koin.androidContext
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
            modules(
                localModule,
                viewModule
            )
        }
    }
}