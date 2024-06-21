package com.zktony.serialport.utils

import android.os.Build
import android.util.Log
import com.zktony.serialport.ext.toHexString
import com.zktony.serialport.utils.writeThread
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.Executors

//import com.zktony.android.BuildConfig

/**
 * @author 刘贺贺
 * @date 2023/9/15 14:14
 */

fun log(tag: String, message: String) {
    Log.i(tag, message)
    writeThread(
        "$tag;$message"
    )
}
