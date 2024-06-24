package com.zktony.android

import android.app.Application
import com.zktony.android.utils.ApplicationUtils
import com.zktony.android.utils.PromptSoundUtils
import com.zktony.android.utils.HzmctUtils
import com.zktony.android.utils.SerialPortUtils
import com.zktony.android.utils.StorageUtils
import com.zktony.log.LogUtils
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
        StorageUtils.with(this)
        LogUtils.with(this)
        ApplicationUtils.with(this)
        HzmctUtils.with(this)
        PromptSoundUtils.with(this)

        // Initialize the application without context
        SerialPortUtils.with()
    }
}