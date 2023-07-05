package com.zktony.android.core.utils

/**
 * @author 刘贺贺
 * @date 2023/6/30 9:27
 */
enum class ControlType {
    /**
     * 复位 00
     */
    CONTROL_RESET,

    /**
     * 运行 01
     */
    CONTROL_MOVE,

    /**
     * 停止 02
     */
    CONTROL_STOP,

    /**
     * 查询轴状态 03
     */
    CONTROL_QUERY_AXIS,

    /**
     * 查询GPIO状态 04
     */
    CONTROL_QUERY_GPIO,

    /**
     * 切阀 05
     */
    CONTROL_VALVE,
}