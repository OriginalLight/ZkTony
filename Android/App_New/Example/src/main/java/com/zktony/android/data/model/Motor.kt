package com.zktony.android.data.model

import androidx.compose.runtime.Immutable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zktony.android.ext.nextId
import com.zktony.serialport.ext.writeInt32LE
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
data class Motor(
    @PrimaryKey @ColumnInfo(name = "id") val id: Long = nextId(),
    @ColumnInfo(name = "index") val index: Int = 0,
    @ColumnInfo(name = "text") val text: String = "",
    @ColumnInfo(name = "speed") val speed: Long = 600L,
    @ColumnInfo(name = "acc") val acc: Long = 300L,
    @ColumnInfo(name = "dec") val dec: Long = 400L,
    @ColumnInfo(name = "create_time") val createTime: Date = Date(System.currentTimeMillis()),
) {
    fun toByteArray(): ByteArray {
        val ba = ByteArray(12)
        return ba.writeInt32LE(acc, 0).writeInt32LE(dec, 4).writeInt32LE(speed, 8)
    }
}