package com.zktony.serialport.lifecycle

import com.zktony.serialport.SerialPortImpl

object SerialStoreUtils {

    // The store of serial port objects
    private val hashMap = hashMapOf<String, SerialPortImpl>()

    /**
     * Add a serial port object to the store
     * @param key The key is the serial port name
     * @param value The value is the serial port object
     */
    fun put(key: String, value: SerialPortImpl) {
        val old = hashMap.put(key, value)
        old?.close()
    }

    /**
     * Get the serial port object from the store
     * @param key The key is the serial port name
     */
    fun get(key: String): SerialPortImpl? {
        return hashMap[key]
    }

    /**
     * Get all serial port names
     */
    fun keys(): Set<String> {
        return HashSet(hashMap.keys)
    }

    /**
     * Get all serial port objects
     */
    fun values(): Collection<SerialPortImpl> {
        return hashMap.values
    }

    /**
     * Remove the serial port object from the store
     */
    fun remove(key: String) {
        hashMap.remove(key)?.close()
    }

    /**
     * Clear all serial port objects
     */
    fun clear() {
        for (ss in hashMap.values) {
            ss.close()
        }
        hashMap.clear()
    }
}