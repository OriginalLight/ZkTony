package com.zktony.log

import android.app.Application
import android.util.Log
import java.io.File

object LogUtils {
    private const val TAG = "AppLog"
    private lateinit var service: LogStorageService

    fun with(app: Application) {
        service = LogStorageService(app)
    }

    // 导出
    fun getLogs(): List<File> {
        return service.getLogs()
    }

    // Error日志级别
    fun error(msg: String, storage: Boolean = false) {
        Log.e(TAG, msg)
        if (storage) {
            service.writeLog("[E/$TAG] $msg")
        }
    }

    // Info日志级别
    fun info(msg: String, storage: Boolean = false) {
        Log.i(TAG, msg)
        if (storage) {
            service.writeLog("[I/$TAG] $msg")
        }
    }

    // Debug日志级别
    fun debug(msg: String, storage: Boolean = false) {
        Log.d(TAG, msg)
        if (storage) {
            service.writeLog("[D/$TAG] $msg")
        }
    }

    // Warn日志级别
    fun warn(msg: String, storage: Boolean = false) {
        Log.w(TAG, msg)
        if (storage) {
            service.writeLog("[W/$TAG] $msg")
        }
    }

    // Verbose日志级别
    fun verbose(msg: String, storage: Boolean = false) {
        Log.v(TAG, msg)
        if (storage) {
            service.writeLog("[V/$TAG] $msg")
        }
    }

    // WTF日志级别
    fun wtf(msg: String, storage: Boolean = false) {
        Log.wtf(TAG, msg)
        if (storage) {
            service.writeLog("[WTF/$TAG] $msg")
        }
    }

    // Error日志级别
    fun error(tag: String, msg: String, storage: Boolean = false) {
        Log.e(tag, msg)
        if (storage) {
            service.writeLog("[E/$tag] $msg")
        }
    }

    // Info日志级别
    fun info(tag: String, msg: String, storage: Boolean = false) {
        Log.i(tag, msg)
        if (storage) {
            service.writeLog("[I/$tag] $msg")
        }
    }

    // Debug日志级别
    fun debug(tag: String, msg: String, storage: Boolean = false) {
        Log.d(tag, msg)
        if (storage) {
            service.writeLog("[D/$tag] $msg")
        }
    }

    // Warn日志级别
    fun warn(tag: String, msg: String, storage: Boolean = false) {
        Log.w(tag, msg)
        if (storage) {
            service.writeLog("[W/$tag] $msg")
        }
    }

    // Verbose日志级别
    fun verbose(tag: String, msg: String, storage: Boolean = false) {
        Log.v(tag, msg)
        if (storage) {
            service.writeLog("[V/$tag] $msg")
        }
    }

    // WTF日志级别
    fun wtf(tag: String, msg: String, storage: Boolean = false) {
        Log.wtf(tag, msg)
        if (storage) {
            service.writeLog("[WTF/$tag] $msg")
        }
    }
}