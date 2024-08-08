package com.zktony.android

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.HzmctUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.work.utils.WorkUtils
import com.zktony.log.LogUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * The main application class that initializes the application and its dependencies.
 */
@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Initializes the application and its dependencies.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize the application context
        ApplicationUtils.with(this)
        LogUtils.with(this)
        WorkUtils.with(this)

        // Initialize the application without context
        HzmctUtils.with()
        SerialPortUtils.with()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}