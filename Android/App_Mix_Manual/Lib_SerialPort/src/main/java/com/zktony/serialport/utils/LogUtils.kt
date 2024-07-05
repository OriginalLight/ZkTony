package com.zktony.serialport.utils

import android.util.Log


fun logInfo(tag: String, message: String) {
    Log.i(tag, message)
    writeThread(
        "$tag;$message"
    )
}
