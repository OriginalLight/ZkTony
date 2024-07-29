package com.zktony.android.utils.extra

import com.zktony.room.entities.Program

fun List<Program>.itemsEqual(other: List<Program>): Boolean {
    if (this.size != other.size) {
        return false
    }
    val ids1 = this.map { it.id }
    val ids2 = other.map { it.id }
    ids1.forEach {
        if (!ids2.contains(it)) {
            return false
        }
    }
    return true
}