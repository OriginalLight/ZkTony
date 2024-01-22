package com.zktony.android.data.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2023-02-02 10:56
 */
@Entity(tableName = "setting")
@Immutable
data class Setting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    //===============配件寿命==============================
    /**
     *  高浓度泵使用的时间
     */
    var highTime: Double = 0.0,

    /**
     *  低浓度泵使用的时间
     */
    var lowLife: Double = 0.0,

    /**
     *  高浓度泵预计使用时间
     */
    var rinseTime: Double = 0.0,


    /**
     *  高浓度泵预计使用时间
     */
    var highTimeExpected: Double = 500.0,

    /**
     *  低浓度泵预计使用时间
     */
    var lowTimeExpected: Double = 500.0,

    /**
     *  冲洗液泵预计使用时间
     */
    var rinseTimeExpected: Double = 500.0,

    //===============配件寿命==============================

    //===============位置设置==============================

    /**
     *  废液位置
     */
    var wastePosition: Double = 0.0,

    /**
     *  胶板位置
     */
    var glueBoardPosition: Double = 0.0,


    //===============位置设置==============================


    //================预排设置=============================


    /**
     *  高浓度清洗液量
     */
    var higeCleanVolume: Double = 0.0,

    /**
     *  高浓度预排液量
     */
    var higeRehearsalVolume: Double = 0.0,

    /**
     *  高浓度管路填充
     */
    var higeFilling: Double = 0.0,

    /**
     *  低浓度清洗液量
     */
    var lowCleanVolume: Double = 0.0,

    /**
     *  低浓度管路填充
     */
    var lowFilling: Double = 0.0,

    /**
     *  冲洗液泵清洗液量
     */
    var rinseCleanVolume: Double = 0.0,

    /**
     *  冲洗液泵管路填充
     */
    var rinseFilling: Double = 0.0,

    /**
     *  促凝剂泵清洗液量
     */
    var coagulantCleanVolume: Double = 0.0,

    /**
     *  促凝剂泵管路填充
     */
    var coagulantFilling: Double = 0.0,

    //================预排设置=============================

)