package com.zktony.serialport.lifecycle

import com.zktony.serialport.AbstractSerialHelper
import com.zktony.serialport.lifecycle.SerialStoreOwner.serialStore

object SerialStoreUtils {
    fun put(key: String, value: AbstractSerialHelper) {
        serialStore.put(key, value)
    }

    fun get(key: String): AbstractSerialHelper? {
        return serialStore.get(key)
    }

    fun remove(key: String) {
        serialStore.remove(key)
    }

    fun clear() {
        serialStore.clear()
    }
}