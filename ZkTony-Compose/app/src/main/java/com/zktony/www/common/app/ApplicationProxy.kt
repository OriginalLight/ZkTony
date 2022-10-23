package com.zktony.www.common.app

import android.app.Application

interface ApplicationProxy {

    fun onCreate(application: Application)

    fun onTerminate()

}