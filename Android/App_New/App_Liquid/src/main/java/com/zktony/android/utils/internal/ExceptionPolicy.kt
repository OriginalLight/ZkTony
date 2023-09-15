package com.zktony.android.utils.internal

/**
 * 异常处理策略
 *
 * @author 刘贺贺
 * @date 2023/6/7 14:23
 */
object ExceptionPolicy {
    /**
     * 重试
     */
    const val RETRY = 0

    /**
     * 查询
     */
    const val QUERY = 1

    /**
     * 复位
     */
    const val RESET = 2

    /**
     * 跳过
     */
    const val SKIP = 3

    /**
     * 抛出异常
     */
    const val THROW = 4
}