package com.zktony.serialport.lifecycle

sealed class SerialState {
    data class Success(val byteArray: ByteArray) : SerialState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Success

            return byteArray.contentEquals(other.byteArray)
        }

        override fun hashCode(): Int {
            return byteArray.contentHashCode()
        }
    }

    data class Err(val ex: Exception) : SerialState()
}