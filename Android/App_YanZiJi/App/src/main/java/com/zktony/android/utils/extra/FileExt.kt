package com.zktony.android.utils.extra

import java.io.File

/**
 * 获取文件大小
 */
fun File.size(): String {
    val size = length()
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> {
            val kbSize = size / 1024.0
            if (kbSize % 1 == 0.0) {
                "$kbSize KB"
            } else {
                "%.1f KB".format(kbSize)
            }
        }

        size < 1024 * 1024 * 1024 -> {
            val mbSize = size / 1024.0 / 1024.0
            if (mbSize % 1 == 0.0) {
                "$mbSize MB"
            } else {
                "%.1f MB".format(mbSize)
            }
        }

        else -> {
            val gbSize = size / 1024.0 / 1024.0 / 1024.0
            if (gbSize % 1 == 0.0) {
                "$gbSize GB"
            } else {
                "%.1f GB".format(gbSize)
            }
        }
    }
}