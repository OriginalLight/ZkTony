package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zktony.android.ext.nextId
import com.zktony.serialport.ext.writeFloatLE
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
    @ColumnInfo(name = "speed") val speed: Float = 600f,
    @ColumnInfo(name = "acc") val acc: Float = 300f,
    @ColumnInfo(name = "dec") val dec: Float = 400f,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
) {
    fun toByteArray(): ByteArray {
        val ba = ByteArray(12)
        return ba.writeFloatLE(acc, 0).writeFloatLE(dec, 4).writeFloatLE(speed, 8)
    }
}