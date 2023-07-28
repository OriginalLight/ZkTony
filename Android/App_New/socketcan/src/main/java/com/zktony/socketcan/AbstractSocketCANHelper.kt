package com.zktony.socketcan

import android.util.Log

/**
 * @author 刘贺贺
 * @date 2023/7/28 13:32
 */
abstract class AbstractSocketCANHelper {

    private var fd: Int = -1
    private val socketCAN: SocketCAN = SocketCAN()
    private var readThread: ReadThread? = null
    var callback: (CanFrame) -> Unit = { _ -> }

    /**
     * Open the socketcan
     */
    fun open(channel: String): Int {
        return try {
            fd = socketCAN.open(channel)
            readThread = ReadThread()
            readThread?.start()
            0
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Close the socketcan
     */
    fun close(): Int {
        return try {
            socketCAN.close(fd)
            0
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }

    /**
     * Write data to socketcan
     */
    fun write(id: Long, eff: Long, rtr: Long, len: Int, data: IntArray): Int {
        return try {
            socketCAN.write(fd, id, eff, rtr, len, data)
            0
        } catch (e: Exception) {
            e.printStackTrace()
            -1
        }
    }


    private inner class ReadThread : Thread() {
        override fun run() {
            super.run()
            while (!isInterrupted) {
                try {
                    val frame = socketCAN.read(fd)
                    Log.e("SocketCAN", frame.contentToString())
                    if (frame.isNotEmpty()) {
                        val len = frame[3].toInt()
                        val data = frame.copyOfRange(4, 4 + len)
                        callback.invoke(CanFrame(frame[0], frame[1], frame[2], frame[3], data))
                    }
                    try {
                        sleep(4L)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    return
                }
            }
        }
    }
}