package com.zktony.manager

import android.app.Application
import com.zktony.manager.ui.utils.DataManager
import com.zktony.manager.ui.utils.PermissionManager

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 12:53
 */
class App: Application() {

    lateinit var permissions: PermissionManager
    override fun onCreate() {
        super.onCreate()
        DataManager.provide(this)
        permissions = PermissionManager(this)
    }
}