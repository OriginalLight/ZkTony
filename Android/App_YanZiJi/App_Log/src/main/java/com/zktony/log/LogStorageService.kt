package com.zktony.log

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.LinkedTransferQueue
import java.util.concurrent.TimeUnit

class LogStorageService(ctx: Context) {
    private val executor = Executors.newSingleThreadScheduledExecutor()
    private val queue = LinkedTransferQueue<String>()
    private val dir: File = File(ctx.cacheDir, "logs").let { dir ->
        if (!dir.exists()) {
            dir.mkdirs()
        }
        dir
    }
    private val file: File = File(dir, "${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}.log").let {
        if (!it.exists()) {
            it.createNewFile()
        }
        it
    }

    init {
        try {
            clearLog()
            setLogWriteJob()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writeLog(msg: String) {
        try {
            val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())
            queue.add("$time $msg\n")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLogs(): List<File> {
        return dir.listFiles()?.toList() ?: emptyList()
    }

    private fun setLogWriteJob() {
        executor.scheduleWithFixedDelay({
            val messages = mutableListOf<String>()
            queue.drainTo(messages)
            if (messages.size > 0) {
                try {
                    synchronized(file) {
                        file.appendText(buildString {
                            messages.forEach { append(it) }
                        })
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }, 5, 5, TimeUnit.SECONDS)
    }

    private fun clearLog() {
        executor.execute {
            // 删除7天前的日志
            dir.listFiles()?.forEach {
                if (System.currentTimeMillis() - it.lastModified() > 15 * 24 * 60 * 60 * 1000L) {
                    it.delete()
                    writeLog("DELETE ${it.name}")
                }
            }
        }
    }
}