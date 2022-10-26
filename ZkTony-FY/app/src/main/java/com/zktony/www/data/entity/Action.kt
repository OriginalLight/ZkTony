package com.zktony.www.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 9:56
 */

@Entity
data class Action(
    @PrimaryKey
    var id: String = UUID.randomUUID().toString(),
    var programId: String = "",
    var mode: Int = 0,
    var order: Int = 0,
    var temperature: Float = 0f,
    var liquidVolume: Float = 0f,
    var count: Int = 0,
    var time: Float = 0f,
    var upload: Int = 0,
    var createTime: Date = Date(System.currentTimeMillis())
)

enum class ActionEnum(val index: Int, val str: String) {
    BLOCKING_LIQUID(0, "封闭液"),
    ANTIBODY_ONE(1, "抗体1"),
    ANTIBODY_TWO(2, "抗体2"),
    WASHING(3, "洗涤");

}

fun getActionEnum(index: Int): ActionEnum {
    for (actionEnum in ActionEnum.values()) {
        if (actionEnum.index == index) {
            return actionEnum
        }
    }
    return ActionEnum.BLOCKING_LIQUID
}