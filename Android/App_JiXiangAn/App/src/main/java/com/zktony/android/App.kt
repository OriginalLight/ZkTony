package com.zktony.android

import android.app.Application
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.log.LogUtils
import com.zktony.room.repository.FaultRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * The main application class that initializes the application and its dependencies.
 */
@HiltAndroidApp
class App : Application() {

    @Inject
    lateinit var faultRepository: FaultRepository
    /**
     * Initializes the application and its dependencies.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize the application context
        LogUtils.with(this, faultRepository)
        ApplicationUtils.with(this)
        SerialPortUtils.with()
    }
}