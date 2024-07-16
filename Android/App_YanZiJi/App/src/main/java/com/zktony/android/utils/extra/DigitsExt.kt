package com.zktony.android.utils.extra

import java.math.BigDecimal

fun Double.format(digits: Int) = "%.${digits}f".format(this)

fun Float.format(digits: Int) = "%.${digits}f".format(this)

fun String.toDoubleOrDefault() = this.toDoubleOrNull() ?: 0.0

fun String.toFloatOrDefault() = this.toFloatOrNull() ?: 0.0f

fun String.toIntOrDefault() = this.toIntOrNull() ?: 0