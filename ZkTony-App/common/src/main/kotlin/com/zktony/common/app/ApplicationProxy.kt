package com.zktony.common.app

import android.app.Application

interface ApplicationProxy {

    fun onCreate(application: Application)

    fun onTerminate()

}