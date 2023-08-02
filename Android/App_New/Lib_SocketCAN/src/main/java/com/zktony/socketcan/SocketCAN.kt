package com.zktony.socketcan

class SocketCAN {

    /**
     * A native method that is implemented by the 'socketcan' native library,
     * which is packaged with this application.
     */
    external fun open(channel: String): Int

    external fun close(fd: Int): Int

    external fun write(fd: Int, id: Long, eff: Long, rtr: Long, len: Int, data: IntArray): Int

    external fun read(fd: Int): LongArray

    companion object {
        // Used to load the 'socketcan' library on application startup.
        init {
            System.loadLibrary("socketcan")
        }

        private const val TAG = "SocketCAN"
    }
}