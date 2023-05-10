package com.zktony.android.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 10:42
 */
@Entity(tableName = "calibration")
data class Calibration(
    @PrimaryKey
    val id: Long = nextId(),
    // 校准名称
    val name: String = "默认",
    val x: Float = 10f,
    val y: Float = 10f,
    val z: Float = 10f,
    val v1: Float = 100f,
    val v2: Float = 100f,
    val v3: Float = 100f,
    val v4: Float = 100f,
    val v5: Float = 100f,
    val v6: Float = 100f,
    val v7: Float = 100f,
    val v8: Float = 100f,
    val v9: Float = 100f,
    val v10: Float = 100f,
    val v11: Float = 100f,
    val v12: Float = 100f,
    val v13: Float = 100f,
    // 是否选用
    val active: Int = 0,
    // 创建时间
    val createTime: Date = Date(System.currentTimeMillis()),
)
