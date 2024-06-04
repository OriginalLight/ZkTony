package com.zktony.serialport.lifecycle

import com.zktony.serialport.SerialPortImpl

object SerialStoreUtils {

    // The store of serial port objects
    private val store = SerialStore()

    /**
     * Add a serial port object to the store
     * @param key The key is the serial port name
     * @param value The value is the serial port object
     */
    fun put(key: String, value: SerialPortImpl) {
        store.put(key, value)
    }

    /**
     * Get the serial port object from the store
     * @param key The key is the serial port name
     */
    fun get(key: String): SerialPortImpl? {
        return store.get(key)
    }

    /**
     * Get all serial port names
     */
    fun keys(): Set<String> {
        return store.keys()
    }

    /**
     * Get all serial port objects
     */
    fun values(): Collection<SerialPortImpl> {
        return store.values()
    }

    /**
     * Remove the serial port object from the store
     */
    fun remove(key: String) {
        store.remove(key)
    }

    /**
     * Clear all serial port objects
     */
    fun clear() {
        store.clear()
    }
}