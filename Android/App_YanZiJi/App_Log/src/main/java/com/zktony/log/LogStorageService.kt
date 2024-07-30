package com.zktony.log

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class LogStorageService(ctx: Context) {
    private val logDir: File = File(ctx.cacheDir, "logs")
    private val logFileName: String =
        "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.log"
    private val executor = Executors.newSingleThreadExecutor()

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        clearLog()
    }

    fun writeLog(msg: String) {
        executor.execute {
            val time =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            val message = "$time: $msg\n"
            try {
                val logFile = File(logDir, logFileName)
                synchronized(logFile) {
                    FileWriter(logFile, true).use {
                        it.write(message)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getLogs(): List<File> {
        return logDir.listFiles()?.toList() ?: emptyList()
    }

    private fun clearLog() {
        executor.execute {
            // 删除7天前的日志
            val files = logDir.listFiles()
            files?.forEach {
                val date = SimpleDateFormat(
                    "yyyy-MM-dd",
                    Locale.getDefault()
                ).parse(it.nameWithoutExtension)
                if (date != null && System.currentTimeMillis() - date.time > 7 * 24 * 60 * 60 * 1000) {
                    it.delete()
                }
            }
        }
    }
}