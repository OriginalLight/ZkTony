package com.zktony.socketcan

/**
 * @author 刘贺贺
 * @date 2023/7/28 14:05
 */
data class CanFrame(
    val id: Long,
    val eff: Long,
    val rtr: Long,
    val len: Long,
    val data: LongArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CanFrame

        if (id != other.id) return false
        if (eff != other.eff) return false
        if (rtr != other.rtr) return false
        if (len != other.len) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + eff.hashCode()
        result = 31 * result + rtr.hashCode()
        result = 31 * result + len.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

}