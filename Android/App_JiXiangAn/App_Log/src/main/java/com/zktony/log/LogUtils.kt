package com.zktony.log

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.zktony.log.storage.FileStorage
import com.zktony.room.entities.Fault
import com.zktony.room.repository.FaultRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
object LogUtils {
    const val TAG = "AppLog"
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var ctx: Application
    lateinit var fileStorage: FileStorage
    lateinit var repository: FaultRepository

    fun with(app: Application, repo: FaultRepository) {
        this.ctx = app
        fileStorage = FileStorage(app)
        repository = repo
    }

    fun error(msg: String, storage: Boolean = false) {
        Log.e(TAG, msg)
        if (storage) {
            fileStorage.writeLog("[E/$TAG] $msg")
        }
    }

    fun info(msg: String, storage: Boolean = false) {
        Log.i(TAG, msg)
        if (storage) {
            fileStorage.writeLog("[I/$TAG] $msg")
        }
    }

    fun debug(msg: String, storage: Boolean = false) {
        Log.d(TAG, msg)
        if (storage) {
            fileStorage.writeLog("[D/$TAG] $msg")
        }
    }

    fun warn(msg: String, storage: Boolean = false) {
        Log.w(TAG, msg)
        if (storage) {
            fileStorage.writeLog("[W/$TAG] $msg")
        }
    }

    fun verbose(msg: String, storage: Boolean = false) {
        Log.v(TAG, msg)
        if (storage) {
            fileStorage.writeLog("[V/$TAG] $msg")
        }
    }

    fun wtf(msg: String, storage: Boolean = false) {
        Log.wtf(TAG, msg)
        if (storage) {
            fileStorage.writeLog("[WTF/$TAG] $msg")
        }
    }

    fun error(tag: String, msg: String, storage: Boolean = false) {
        Log.e(tag, msg)
        if (storage) {
            fileStorage.writeLog("[E/$tag] $msg")
        }
    }

    fun info(tag: String, msg: String, storage: Boolean = false) {
        Log.i(tag, msg)
        if (storage) {
            fileStorage.writeLog("[I/$tag] $msg")
        }
    }

    fun debug(tag: String, msg: String, storage: Boolean = false) {
        Log.d(tag, msg)
        if (storage) {
            fileStorage.writeLog("[D/$tag] $msg")
        }
    }

    fun warn(tag: String, msg: String, storage: Boolean = false) {
        Log.w(tag, msg)
        if (storage) {
            fileStorage.writeLog("[W/$tag] $msg")
        }
    }

    fun verbose(tag: String, msg: String, storage: Boolean = false) {
        Log.v(tag, msg)
        if (storage) {
            fileStorage.writeLog("[V/$tag] $msg")
        }
    }

    fun wtf(tag: String, msg: String, storage: Boolean = false) {
        Log.wtf(tag, msg)
        if (storage) {
            fileStorage.writeLog("[WTF/$tag] $msg")
        }
    }

    fun fault(fault: Fault, storage: Boolean = false) {
        Log.e(TAG, "FAULT: ${fault.code} - ${fault.description}")
        if (storage) {
            fileStorage.writeLog("[E/$TAG] FAULT: ${fault.code} - ${fault.description}")
        }
        scope.launch {
            repository.insert(fault)
        }
    }
}