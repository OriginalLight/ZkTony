package com.zktony.android.logic.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Immutable
import com.zktony.core.ext.nextId
import com.zktony.serialport.ext.writeInt16LE
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:27
 */
@Entity(
    tableName = "motors",
    indices = [
        Index(value = ["text"], unique = true)
    ]
)
@Immutable
data class MotorEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long = nextId(),
    @ColumnInfo(name = "index") val index: Int = 0,
    @ColumnInfo(name = "text") val text: String = "",
    @ColumnInfo(name = "speed") val speed: Int = 600,
    @ColumnInfo(name = "acc") val acc: Int = 300,
    @ColumnInfo(name = "dec") val dec: Int = 400,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
) {
    fun toByteArray(): ByteArray {
        val ba = ByteArray(6)
        return ba.writeInt16LE(acc, 0).writeInt16LE(dec, 2).writeInt16LE(speed, 4)
    }
}