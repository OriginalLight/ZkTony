package com.zktony.serialport.lifecycle

import com.zktony.serialport.AbstractSerialPort

open class SerialStore {
    // The key is the serial port name, and the value is the serial port object
    private val map = mutableMapOf<String, AbstractSerialPort>()

    /**
     * Add a serial port object to the store
     *
     * @param key   The key is the serial port name
     * @param value The value is the serial port object
     */
    fun put(key: String, value: AbstractSerialPort) {
        val old = map.put(key, value)
        old?.close()
    }

    /**
     * Get the serial port object from the store
     *
     * @param key The key is the serial port name
     * @return The value is the serial port object
     */
    fun get(key: String): AbstractSerialPort? {
        return map[key]
    }

    /**
     * Get all serial port names
     */
    fun keys(): Set<String> {
        return HashSet(map.keys)
    }

    /**
     * Get all serial port objects
     */
    fun values(): Collection<AbstractSerialPort> {
        return map.values
    }

    /**
     * Remove the serial port object from the store
     */
    fun remove(key: String) {
        map.remove(key)?.close()
    }

    /**
     * Clear all serial port objects
     */
    fun clear() {
        for (ss in map.values) {
            ss.close()
        }
        map.clear()
    }
}