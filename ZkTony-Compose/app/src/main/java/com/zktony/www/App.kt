package com.zktony.www

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zktony.www.common.datastore.DataStoreFactory
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

    override fun onCreate() {
        appContext = applicationContext
        super.onCreate()
        DataStoreFactory.init(this)
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(workerFactory)
            .build()
}