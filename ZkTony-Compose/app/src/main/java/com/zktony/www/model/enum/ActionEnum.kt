package com.zktony.www.model.enum

/**
 * @author: 刘贺贺
 * @date: 2022-09-30 10:09
 */
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