@file:Suppress("DEPRECATION")

package com.zktony.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Handler
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import java.util.UUID


class SpBLE(
    private val context: Context,
    private val handler: Handler,
    private val bluetoothAdapter: BluetoothAdapter? = (getSystemService(
        context,
        BluetoothManager::class.java
    ))?.adapter
) {
    val STATE_DISCONNECTED = 0
    val STATE_CONNECTING = 2
    val STATE_CONNECTED = 3

    // UUIDs for UAT service and associated characteristics.
    var UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    var TX_UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")
    var RX_UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")

    // UUID for the BTLE client characteristic which is necessary for notifications.
    var CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var gatt: BluetoothGatt? = null
    private var tx: BluetoothGattCharacteristic? = null
    private var rx: BluetoothGattCharacteristic? = null
    var connectionState = STATE_DISCONNECTED
    var bluetoothDeviceAddress: String? = null
    var bluetoothDeviceName: String? = null

    private var retryCount = 0

    fun setUUID(uart: UUID, txd: UUID, rxd: UUID) {
        UART_UUID = uart
        TX_UUID = txd
        RX_UUID = rxd
    }

    fun connectDevice(bluetoothDevice: BluetoothDevice) {
        gatt = bluetoothDevice.connectGatt(context, false, callback)
    }

    fun connect(address: String?, deviceName: String) {
        if (bluetoothAdapter == null || address == null) {
            return
        }
        val device: BluetoothDevice = bluetoothAdapter
            .getRemoteDevice(address) ?: return
        bluetoothDeviceAddress = address
        bluetoothDeviceName = deviceName
        gatt = device.connectGatt(this.context, false, callback)
    }

    fun connect(device: BluetoothDevice?) {
        if (device == null) {
            return
        }
        connectionState = STATE_CONNECTING
        handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, connectionState, -1)
            .sendToTarget()
        bluetoothDeviceAddress = device.address
        bluetoothDeviceName = device.name
        retryCount = 0
        gatt = device.connectGatt(this.context, false, callback)
    }

    fun getRSSI(): Int {
        if (gatt == null) return 0
        gatt!!.readRemoteRssi()
        return 0
    }

    // Main BTLE device callback where much of the logic occurs.
    private val callback: BluetoothGattCallback = object : BluetoothGattCallback() {
        // Called whenever the device connection state changes, i.e. from disconnected to connected.
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d(TAG, "Connected!")
                connectionState = STATE_CONNECTED
                handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, connectionState, -1)
                    .sendToTarget()
                // Discover services.
                if (!gatt.discoverServices()) {
                    Log.e(TAG, "Failed to start discovering services!")
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected!")
                connectionState = STATE_DISCONNECTED
                handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, connectionState, -1)
                    .sendToTarget()
            } else {
                Log.d(TAG, "Connection state changed.  New state: $newState")
            }
        }

        // Called when services have been discovered on the remote device.
        // It seems to be necessary to wait for this discovery to occur before
        // manipulating any services or characteristics.
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Service discovery completed!")
            } else {
                Log.e(TAG, "Service discovery failed with status: $status")
            }
            if (UART_UUID == null) {
                Log.e(TAG, "uart UUID error")
                return
            }
            if (gatt.getService(UART_UUID) == null) {
                Log.e(TAG, "service error")
                // Discover services.
                handler.obtainMessage(101, 0, -1).sendToTarget()
                if (retryCount++ < 10) if (!gatt.discoverServices()) {
                    Log.e(TAG, "Failed to start discovering services!")
                }
                return
            }
            // Save reference to each characteristic.
            tx = gatt.getService(UART_UUID).getCharacteristic(TX_UUID)
            rx = gatt.getService(UART_UUID).getCharacteristic(RX_UUID)
            if (tx != null) {
                val charaProp = tx!!.properties
                // 可读
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {
                }
                // 可写，注：要 & 其可写的两个属性
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0
                    || charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0
                ) {
                }
                // 可通知，可指示
                if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0
                    || charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0
                ) {
                }
            }
            // Setup notifications on RX characteristic changes (i.e. data received).
            // First call setCharacteristicNotification to enable notification.
            if (!gatt.setCharacteristicNotification(rx, true)) {
                Log.w(TAG, "Couldn't set notifications for RX characteristic!")
            }
            // Next update the RX characteristic's client descriptor to enable notifications.
            if (rx != null) {
                if (rx!!.getDescriptor(CLIENT_UUID) != null) {
                    val desc = rx!!.getDescriptor(CLIENT_UUID)
                    desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    if (!gatt.writeDescriptor(desc)) {
                        Log.e(TAG, "Couldn't write RX client descriptor value!")
                        handler.obtainMessage(101, 1, -1).sendToTarget()
                    }
                } else {
                    Log.e(TAG, "Couldn't get RX client descriptor!")
                    handler.obtainMessage(101, 1, -1).sendToTarget()
                }
            } else {
                Log.e(TAG, "Couldn't find RX characteristic!")
                handler.obtainMessage(101, 1, -1).sendToTarget()
            }

        }

        // Called when a remote characteristic changes (like the RX characteristic).
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            super.onCharacteristicChanged(gatt, characteristic)
            val data = characteristic.value
            handler.obtainMessage(Constants.MESSAGE_READ, data.size, -1, data)
                .sendToTarget()
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(status, rssi);
                handler.obtainMessage(100, rssi, -1).sendToTarget()
            }
        }
    }

    fun write(data: ByteArray) {
        // Update TX characteristic value.  Note the setValue overload that takes a byte array must be used.
        if (tx == null) return
        tx!!.value = data
        if (gatt!!.writeCharacteristic(tx)) {
            handler.obtainMessage(Constants.MESSAGE_WRITE, data.size, -1, data)
                .sendToTarget()
            Log.d(TAG, "Sent: ${data.size} bytes: ${data.toString(Charsets.UTF_8)}")
        } else {
            Log.e(TAG, "Couldn't write TX characteristic!")
        }
    }

    fun disconnect() {
        if (gatt != null) {
            // For better reliability be careful to disconnect and close the connection.
            gatt!!.disconnect()
            gatt!!.close()
            gatt = null
            tx = null
            rx = null
            connectionState = STATE_DISCONNECTED
            handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, connectionState, -1)
                .sendToTarget()
        }
    }


    companion object {
        const val TAG = "BluetoothLeService"
    }
}