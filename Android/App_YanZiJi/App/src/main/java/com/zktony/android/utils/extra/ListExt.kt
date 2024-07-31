package com.zktony.android.utils.extra

fun List<Long>.itemsEqual(other: List<Long>): Boolean {
    if (this.size != other.size) {
        return false
    }

    this.forEach {
        if (!other.contains(it)) {
            return false
        }
    }
    return true
}