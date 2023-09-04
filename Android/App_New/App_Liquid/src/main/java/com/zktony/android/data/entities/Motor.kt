package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.serialport.ext.writeInt32LE
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:27
 */
@Entity(tableName = "motor")
@Immutable
data class Motor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val index: Int = 0,
    val speed: Long = 600L,
    val acceleration: Long = 300L,
    val deceleration: Long = 400L,
    val createTime: Date = Date(System.currentTimeMillis())
) {
    fun toAdsString(): Triple<String, String, String> {
        return Triple(acceleration.toString(), deceleration.toString(), speed.toString())
    }

    fun toByteArray(): ByteArray {
        val ba = ByteArray(12)
        ba.writeInt32LE(acceleration, 4)
        ba.writeInt32LE(deceleration, 8)
        ba.writeInt32LE(speed, 0)
        return ba
    }
}