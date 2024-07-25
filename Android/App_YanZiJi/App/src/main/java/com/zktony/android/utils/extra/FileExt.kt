package com.zktony.android.utils.extra

import java.io.File

fun File.size(): String {
    val size = length()
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / 1024 / 1024} MB"
        else -> "${size / 1024 / 1024 / 1024} GB"
    }
}