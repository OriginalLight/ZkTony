package com.zktony.common.utils

/**
 * 雪花算法
 */
class Snowflake(private val workerId: Long) {
    // 机器ID占用位数
    private val workerIdBits = 10L
    // 序列号占用位数
    private val sequenceBits = 12L

    // 机器ID左移位数
    private val workerIdShift = sequenceBits
    private val timestampShift = sequenceBits + workerIdBits
    private val sequenceMask = -1L xor (-1L shl sequenceBits.toInt())

    private var lastTimestamp = -1L
    private var sequence = 0L

    fun nextId(): Long {
        var timestamp = System.currentTimeMillis()
        if (timestamp < lastTimestamp) {
            throw RuntimeException("Clock moved backwards")
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) and sequenceMask
            if (sequence == 0L) {
                timestamp = tilNextMillis(lastTimestamp)
            }
        } else {
            sequence = 0L
        }
        lastTimestamp = timestamp
        return ((timestamp shl timestampShift.toInt()) or (workerId shl workerIdShift.toInt()) or sequence)
    }

    private fun tilNextMillis(lastTimestamp: Long): Long {
        var timestamp = System.currentTimeMillis()
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis()
        }
        return timestamp
    }
}

