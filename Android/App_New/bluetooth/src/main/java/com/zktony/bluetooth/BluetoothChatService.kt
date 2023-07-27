package com.zktony.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
class BluetoothChatService(
    context: Context,
    private val handler: Handler,
    private val bluetoothAdapter: BluetoothAdapter? = (ContextCompat.getSystemService(
        context,
        BluetoothManager::class.java
    ))?.adapter
) {

    private var acceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var connectionState: Int = STATE_NONE

    @get:Synchronized
    @set:Synchronized
    var state: Int
        /**
         * Return the current connection state.  */
        get() = connectionState
        /**
         * Set the current state of the chat connection
         * @param state  An integer defining the current connection state
         */
        private set(state) {
            Log.d(TAG, "setState() $connectionState -> $state")
            connectionState = state

            // Give the new state to the Handler so the UI Activity can update
            handler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget()
        }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()  */
    @Synchronized
    fun start() {
        Log.d(TAG, "start")

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
        state = STATE_LISTEN
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    @Synchronized
    fun connect(device: BluetoothDevice) {
        Log.d(TAG, "connect to: $device")

        // Cancel any thread attempting to make a connection
        if (connectionState == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread!!.cancel()
                connectThread = null
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }

        // Start the thread to connect with the given device
        connectThread = ConnectThread(device)
        connectThread!!.start()
        state = STATE_CONNECTING
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    @Synchronized
    fun connected(socket: BluetoothSocket?, device: BluetoothDevice) {
        Log.d(TAG, "connected")

        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }

        // Cancel the accept thread because we only want to connect to one device
        if (acceptThread != null) {
            acceptThread!!.cancel()
            acceptThread = null
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = ConnectedThread(socket)
        connectedThread!!.start()

        // Send the name of the connected device back to the UI Activity
        val msg = handler.obtainMessage(Constants.MESSAGE_DEVICE_NAME)
        val bundle = Bundle()
        bundle.putString("device_name", device.name)
        msg.data = bundle
        handler.sendMessage(msg)
        state = STATE_CONNECTED
    }

    /**
     * Stop all threads
     */
    @Synchronized
    fun stop() {
        Log.d(TAG, "stop")
        if (connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }
        if (connectedThread != null) {
            connectedThread!!.cancel()
            connectedThread = null
        }
        if (acceptThread != null) {
            acceptThread!!.cancel()
            acceptThread = null
        }
        state = STATE_NONE
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        // Create temporary object
        var r: ConnectedThread?
        // Synchronize a copy of the ConnectedThread
        synchronized(this) {
            if (connectionState != STATE_CONNECTED) return
            r = connectedThread
        }
        // Perform the write unsynchronized
        r!!.write(out)
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private fun connectionFailed() {
        state = STATE_LISTEN

        // Send a failure message back to the Activity
//        val msg = handler.obtainMessage(Constants.MESSAGE_TOAST)
//        val bundle = Bundle()
//        bundle.putString(MainActivity.TOAST, "连接失败")
//        msg.data = bundle
//        handler.sendMessage(msg)
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private fun connectionLost() {
        state = STATE_LISTEN

        // Send a failure message back to the Activity
//        val msg = handler.obtainMessage(Constants.MESSAGE_TOAST)
//        val bundle = Bundle()
//        bundle.putString(MainActivity.TOAST, "蓝牙已断开连接")
//        msg.data = bundle
//        handler.sendMessage(msg)
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private inner class AcceptThread : Thread() {
        // The local server socket
        private val serverSocket: BluetoothServerSocket?

        init {
            var bluetoothServerSocket: BluetoothServerSocket? = null

            // Create a new listening server socket
            try {
                bluetoothServerSocket =
                    bluetoothAdapter?.listenUsingRfcommWithServiceRecord(NAME, MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "listen() failed", e)
            }
            serverSocket = bluetoothServerSocket
        }

        override fun run() {
            Log.d(TAG, "BEGIN acceptThread$this")
            name = "AcceptThread"
            var socket: BluetoothSocket?

            // Listen to the server socket if we're not connected
            while (connectionState != STATE_CONNECTED) {
                socket = try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    serverSocket!!.accept()
                } catch (e: IOException) {
                    Log.e(TAG, "accept() failed", e);
                    break
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized(this@BluetoothChatService) {
                        when (connectionState) {
                            STATE_LISTEN, STATE_CONNECTING ->                             // Situation normal. Start the connected thread.
                                connected(socket, socket.remoteDevice)

                            STATE_NONE, STATE_CONNECTED ->                             // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "Could not close unwanted socket", e)
                                }

                            else -> {}
                        }
                    }
                }
            }
            Log.i(TAG, "END acceptThread")
        }

        fun cancel() {
            Log.d(TAG, "cancel $this")
            try {
                serverSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of server failed", e)
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private inner class ConnectThread(private val device: BluetoothDevice) : Thread() {
        private val bluetoothSocket: BluetoothSocket?

        init {
            var tmp: BluetoothSocket? = null

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "create() failed", e)
            }
            bluetoothSocket = tmp
        }

        override fun run() {
            Log.i(TAG, "BEGIN mConnectThread")
            name = "ConnectThread"

            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter?.cancelDiscovery()

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                bluetoothSocket!!.connect()
            } catch (e: IOException) {
                connectionFailed()
                // Close the socket
                try {
                    bluetoothSocket!!.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2)
                }
                // Start the service over to restart listening mode
                this@BluetoothChatService.start()
                return
            }

            // Reset the ConnectThread because we're done
            synchronized(this@BluetoothChatService) { connectThread = null }

            // Start the connected thread
            connected(bluetoothSocket, device)
        }

        fun cancel() {
            try {
                bluetoothSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private inner class ConnectedThread(socket: BluetoothSocket?) : Thread() {
        private val mmSocket: BluetoothSocket?
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            Log.d(TAG, "create ConnectedThread")
            mmSocket = socket
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket!!.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                Log.e(TAG, "temp sockets not created", e)
            }
            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            Log.i(TAG, "BEGIN connectedThread")
            val buffer = ByteArray(1024)
            var bytes: Int
            var i: Int
            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream!!.read(buffer)
                    if (bytes > 0) {
                        val byteArray = buffer.clone()
                        i = 0
                        while (i < buffer.size) {
                            buffer[i] = 0
                            i++
                        }
                        // Send the obtained bytes to the UI Activity
                        handler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, byteArray)
                            .sendToTarget()
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost()
                    break
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        fun write(buffer: ByteArray) {
            try {
                mmOutStream!!.write(buffer)

                // Share the sent message back to the UI Activity
                handler.obtainMessage(Constants.MESSAGE_WRITE, buffer.size, -1, buffer)
                    .sendToTarget()
            } catch (e: IOException) {
                Log.e(TAG, "Exception during write", e)
            }
        }

        fun cancel() {
            try {
                mmSocket!!.close()
            } catch (e: IOException) {
                Log.e(TAG, "close() of connect socket failed", e)
            }
        }
    }

    companion object {
        // Debugging
        private const val TAG = "BluetoothChatService"

        // Name for the SDP record when creating server socket
        private const val NAME = "BluetoothCom"

        // Unique UUID for this application //Bluetooth serial board
        private val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        // Constants that indicate the current connection state
        const val STATE_NONE = 0 // we're doing nothing
        const val STATE_LISTEN = 1 // now listening for incoming connections
        const val STATE_CONNECTING = 2 // now initiating an outgoing connection
        const val STATE_CONNECTED = 3 // now connected to a remote device
    }
}