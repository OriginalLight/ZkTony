package com.zktony.android.utils

import android.annotation.SuppressLint
import java.io.File


object StorageUtils {
    private const val ARGUMENTS_DIR = "Arguments"
    private const val EXPERIMENTAL_DIR = "Experimental"

    // 获取内部存储路径 /data/data/com.zktony.android/files
    fun getStorageDir(): String {
        return ApplicationUtils.ctx.filesDir.absolutePath
    }

    // 获取缓存路径 /data/data/com.zktony.android/cache
    fun getCacheDir(): String {
        return ApplicationUtils.ctx.cacheDir.absolutePath
    }

    // 获取USB存储路径 /mnt/media_rw
    @SuppressLint("PrivateApi")
    fun getUsbStorageDir(): List<String> {
        val storageDirectories = mutableListOf<String>()
        val storageManager =
            ApplicationUtils.ctx.getSystemService(android.content.Context.STORAGE_SERVICE) as android.os.storage.StorageManager
        val volumeInfoClazz: Class<*>?
        val diskInfoClazz: Class<*>?
        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo")
            diskInfoClazz = Class.forName("android.os.storage.DiskInfo")
            val getVolumes = storageManager.javaClass.getMethod("getVolumes")
            val getDisk = volumeInfoClazz.getMethod("getDisk")
            val getPath = volumeInfoClazz.getMethod("getPath")
            val isUsb = diskInfoClazz.getMethod("isUsb")
            val result = getVolumes.invoke(storageManager) as List<*>

            for (volume in result) {
                val file = getPath.invoke(volume) as File
                val diskInfo = getDisk.invoke(volume)

                if (diskInfo != null) {
                    val isAUsb = isUsb.invoke(diskInfo) as Boolean
                    if (isAUsb) {
                        storageDirectories.add(file.absolutePath.replace("storage", "mnt/media_rw"))
                    }
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        return storageDirectories
    }

    // 获取TF卡存储路径 /mnt/media_rw
    @SuppressLint("PrivateApi")
    fun getTfStorageDir(): List<String> {
        val storageDirectories = mutableListOf<String>()
        val storageManager =
            ApplicationUtils.ctx.getSystemService(android.content.Context.STORAGE_SERVICE) as android.os.storage.StorageManager
        val volumeInfoClazz: Class<*>?
        val diskInfoClazz: Class<*>?

        try {
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo")
            diskInfoClazz = Class.forName("android.os.storage.DiskInfo")
            val getVolumes = storageManager.javaClass.getMethod("getVolumes")
            val getDisk = volumeInfoClazz.getMethod("getDisk")
            val getPath = volumeInfoClazz.getMethod("getPath")
            val isSd = diskInfoClazz.getMethod("isSd")
            val result = getVolumes.invoke(storageManager) as List<*>

            for (volume in result) {
                val file = getPath.invoke(volume) as File
                val diskInfo = getDisk.invoke(volume)

                if (diskInfo != null) {
                    val isATf = isSd.invoke(diskInfo) as Boolean
                    if (isATf) {
                        storageDirectories.add(file.absolutePath.replace("storage", "mnt/media_rw"))
                    }
                }
            }

        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        return storageDirectories
    }

    // 获取参数存储路径
    fun getArgumentDir(): String? {
        val usbList = getUsbStorageDir()
        return if (usbList.isNotEmpty()) {
            "${usbList.first()}/$ARGUMENTS_DIR"
        } else {
            null
        }
    }
}