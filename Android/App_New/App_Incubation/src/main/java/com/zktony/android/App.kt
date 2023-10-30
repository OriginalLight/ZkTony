package com.zktony.android

import android.app.Application
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.SerialPortUtils
import dagger.hilt.android.HiltAndroidApp

/**
 * The main application class that initializes the application and its dependencies.
 */
@HiltAndroidApp
class App : Application() {

    /**
     * Initializes the application and its dependencies.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize the application context
        ApplicationUtils.with(this)
        SerialPortUtils.with()
    }
}