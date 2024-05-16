package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "expected")
@Immutable
data class Expected(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    //================预排设置=============================
    /**
     *  高浓度清洗液量默认值
     */
    var higeCleanDefault: Double = 0.0,

    /**
     *  高浓度预排液量默认值
     */
    var higeRehearsalDefault: Double = 0.0,

    /**
     *  高浓度管路填充默认值
     */
    var higeFillingDefault: Double = 0.0,

    /**
     *  低浓度清洗液量默认值
     */
    var lowCleanDefault: Double = 0.0,

    /**
     *  低浓度管路填充默认值
     */
    var lowFillingDefault: Double = 0.0,

    /**
     *  冲洗液泵清洗液量默认值
     */
    var rinseCleanDefault: Double = 0.0,

    /**
     *  冲洗液泵管路填充默认值
     */
    var rinseFillingDefault: Double = 0.0,

    /**
     *  促凝剂泵清洗液量默认值
     */
    var coagulantCleanDefault: Double = 0.0,

    /**
     *  促凝剂泵管路填充默认值
     */
    var coagulantFillingDefault: Double = 0.0,

    //================预排设置=============================

)