package com.zktony.manager

import android.app.Application
import com.zktony.manager.ui.utils.DataManager

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 12:53
 */
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        DataManager.provide(this)
    }
}