package com.zktony.serialport.lifecycle

import com.zktony.serialport.AbstractSerialHelper

open class SerialStore {
    private val map = mutableMapOf<String, AbstractSerialHelper>()

    fun put(key: String, value: AbstractSerialHelper) {
        val old = map.put(key, value)
        old?.close()
    }

    fun get(key: String): AbstractSerialHelper? {
        return map[key]
    }

    fun keys(): Set<String> {
        return HashSet(map.keys)
    }

    fun values(): Collection<AbstractSerialHelper> {
        return map.values
    }

    fun remove(key: String) {
        map.remove(key)?.close()
    }

    fun clear() {
        for (ss in map.values) {
            ss.close()
        }
        map.clear()
    }
}