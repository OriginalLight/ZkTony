package com.zktony.android

import android.app.Application
import com.zktony.android.di.koinModule
import com.zktony.android.utils.ext.Ext
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * The main application class that initializes the application and its dependencies.
 */
class App : Application() {

    /**
     * Initializes the application and its dependencies.
     */
    override fun onCreate() {
        super.onCreate()

        // Initialize the application context and data store factory
        Ext.with(this)

        // Initialize the dependency injection framework with the application context and modules
        startKoin {
            androidContext(this@App)
            androidLogger(level = Level.INFO)
            modules(koinModule)
        }
    }
}