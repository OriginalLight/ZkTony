package com.zktony.android.logic.utils

/**
 * @author 刘贺贺
 * @date 2023/6/7 14:23
 */
/**
 * 异常处理策略
 */
enum class ExceptionStrategy {
    /**
     * 重试
     */
    RETRY,

    /**
     * 查询
     */
    QUERY,

    /**
     * 复位
     */
    RESET,

    /**
     * 跳过
     */
    SKIP,

    /**
     * 抛出异常
     */
    THROW
}