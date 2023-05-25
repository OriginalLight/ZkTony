package com.zktony.www.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zktony.core.ext.nextId
import java.util.Date

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 9:56
 */

@Entity
data class Action(
    @PrimaryKey
    val id: Long = nextId(),
    val subId: Long = 0L,
    val mode: Int = 0,
    val index: Int = 0,
    val temp: Float = 0f,
    val volume: Float = 0f,
    val count: Int = 0,
    val time: Float = 0f,
    val createTime: Date = Date(System.currentTimeMillis())
)

enum class ActionEnum(val index: Int, val value: String) {
    BLOCKING_LIQUID(0, "封闭液"),
    ANTIBODY_ONE(1, "一抗"),
    ANTIBODY_TWO(2, "二抗"),
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