package com.zktony.www

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zktony.common.app.ApplicationProxy
import com.zktony.common.app.CommonApplicationProxy
import com.zktony.common.dialog.DialogXManager
import com.zktony.www.data.local.datastore.DataStoreFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-09-14 17:25
 */
@HiltAndroidApp
class App : Application(), Configuration.Provider {

    companion object {
        lateinit var appContext: Context
    }

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    private val proxies = listOf<ApplicationProxy>(CommonApplicationProxy)

    override fun onCreate() {
        appContext = applicationContext
        super.onCreate()
        DataStoreFactory.init(this)
        proxies.forEach { it.onCreate(this) }
        DialogXManager(this).init()
    }

    override fun onTerminate() {
        super.onTerminate()
        proxies.forEach { it.onTerminate() }
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()
}