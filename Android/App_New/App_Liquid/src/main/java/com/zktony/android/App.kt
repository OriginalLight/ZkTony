package com.zktony.android

import android.app.Application
import com.zktony.android.utils.extra.Ext
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

        // Initialize the application context and data store factory
        Ext.with(this)
    }
}