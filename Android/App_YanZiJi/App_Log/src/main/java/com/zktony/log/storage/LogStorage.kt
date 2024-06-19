package com.zktony.log.storage

import android.content.Context
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors

class LogStorage(ctx: Context) {
    private val logDir: File = File(ctx.filesDir, "logs")
    private val logFileName: String = "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.txt"
    private val executor = Executors.newSingleThreadExecutor()

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        clearLog()
    }

    fun writeLog(msg: String) {
        executor.execute {
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val message = "$timestamp: $msg\n"
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

    fun readLog(): String {
        val logFile = File(logDir, logFileName)
        return if (logFile.exists()) {
            logFile.readText()
        } else {
            ""
        }
    }

    private fun clearLog() {
        executor.execute {
            // 删除7天前的日志
            val files = logDir.listFiles()
            files?.forEach {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.nameWithoutExtension)
                if (date != null && System.currentTimeMillis() - date.time > 7 * 24 * 60 * 60 * 1000) {
                    it.delete()
                }
            }
        }
    }
}