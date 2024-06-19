package com.zktony.log

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.zktony.log.storage.LogStorage

@SuppressLint("StaticFieldLeak")
object LogUtils {
    private const val TAG = "AppLog"
    private lateinit var ctx: Application
    private lateinit var logStorage: LogStorage

    fun with(app: Application) {
        this.ctx = app
        logStorage = LogStorage(app)
    }

    fun error(msg: String, storage: Boolean = false) {
        Log.e(TAG, msg)
        if (storage) {
            logStorage.writeLog("[E/$TAG] $msg")
        }
    }

    fun info(msg: String, storage: Boolean = false) {
        Log.i(TAG, msg)
        if (storage) {
            logStorage.writeLog("[I/$TAG] $msg")
        }
    }

    fun debug(msg: String, storage: Boolean = false) {
        Log.d(TAG, msg)
        if (storage) {
            logStorage.writeLog("[D/$TAG] $msg")
        }
    }

    fun warn(msg: String, storage: Boolean = false) {
        Log.w(TAG, msg)
        if (storage) {
            logStorage.writeLog("[W/$TAG] $msg")
        }
    }

    fun verbose(msg: String, storage: Boolean = false) {
        Log.v(TAG, msg)
        if (storage) {
            logStorage.writeLog("[V/$TAG] $msg")
        }
    }

    fun wtf(msg: String, storage: Boolean = false) {
        Log.wtf(TAG, msg)
        if (storage) {
            logStorage.writeLog("[WTF/$TAG] $msg")
        }
    }

    fun error(tag: String, msg: String, storage: Boolean = false) {
        Log.e(tag, msg)
        if (storage) {
            logStorage.writeLog("[E/$tag] $msg")
        }
    }

    fun info(tag: String, msg: String, storage: Boolean = false) {
        Log.i(tag, msg)
        if (storage) {
            logStorage.writeLog("[I/$tag] $msg")
        }
    }

    fun debug(tag: String, msg: String, storage: Boolean = false) {
        Log.d(tag, msg)
        if (storage) {
            logStorage.writeLog("[D/$tag] $msg")
        }
    }

    fun warn(tag: String, msg: String, storage: Boolean = false) {
        Log.w(tag, msg)
        if (storage) {
            logStorage.writeLog("[W/$tag] $msg")
        }
    }

    fun verbose(tag: String, msg: String, storage: Boolean = false) {
        Log.v(tag, msg)
        if (storage) {
            logStorage.writeLog("[V/$tag] $msg")
        }
    }

    fun wtf(tag: String, msg: String, storage: Boolean = false) {
        Log.wtf(tag, msg)
        if (storage) {
            logStorage.writeLog("[WTF/$tag] $msg")
        }
    }
}