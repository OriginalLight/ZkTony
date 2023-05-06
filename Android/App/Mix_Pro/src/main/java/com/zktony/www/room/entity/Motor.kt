package com.zktony.www.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.ext.nextId
import com.zktony.serialport.ext.intToHex
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-13 11:27
 */
@Entity(tableName = "motor")
data class Motor(
    @PrimaryKey
    val id: Long = nextId(),
    // 电机编号
    val index: Int = 0,
    // 电机名称
    val text: String = "",
    // 转速
    val speed: Int = 600,
    // 加速度
    val acc: Int = 100,
    // 减速
    val dec: Int = 100,
    // 创建时间
    val createTime: Date = Date(System.currentTimeMillis()),
) {
    /**
     * 生成Hex
     */
    fun hex(): String {
        return speed.intToHex(2) + acc.intToHex(2) + dec.intToHex(2)
    }
}