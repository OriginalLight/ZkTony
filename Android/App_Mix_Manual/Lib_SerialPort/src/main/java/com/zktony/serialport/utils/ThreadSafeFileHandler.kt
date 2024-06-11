package com.zktony.serialport.utils

import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.locks.ReentrantLock

fun writeThread(content: String) {

    val lock = ReentrantLock()

    lock.lock()

    // 获取当前日期
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val file = File("sdcard/Download/log_${currentDate.substringBefore(" ")}.txt")
    if (!file.exists()) {
        if (file.createNewFile()) {
            Log.i("创建日志", "创建${currentDate}日志成功")
        }
    }

    try {
        FileWriter(file, true).use { writer ->
            writer.append("${currentDate}__$content\n")
        }
    } finally {
        lock.unlock()
    }


}