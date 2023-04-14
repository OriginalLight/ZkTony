package com.zktony.gpio

import java.io.File
import java.util.Locale

/**
 * @author 刘贺贺
 */
class Gpio {
    private val mPathstr = "/sys/class/gpio_sw/P"
    private val mDataName = "/data"
    private val mPullName = "/pull"
    private val mDrvLevelName = "/drv"
    private val mMulSelName = "/cfg"

    external fun nativeWriteGpio(path: String, value: String): Int
    external fun nativeReadGpio(path: String): Int

    fun writeGpio(group: Char, num: Int, value: Int): Int {
        val dataPath = composePinPath(group, num) + mDataName
        getPermission(dataPath)
        return nativeWriteGpio(dataPath, value.toString())
    }

    fun readGpio(group: Char, num: Int): Int {
        val dataPath = composePinPath(group, num) + mDataName
        getPermission(dataPath)
        return nativeReadGpio(dataPath)
    }

    fun setPull(group: Char, num: Int, value: Int): Int {
        val dataPath = composePinPath(group, num) + mPullName
        getPermission(dataPath)
        return nativeWriteGpio(dataPath, value.toString())
    }

    fun getPull(group: Char, num: Int): Int {
        val dataPath = composePinPath(group, num) + mPullName
        getPermission(dataPath)
        return nativeReadGpio(dataPath)
    }

    fun setDrvLevel(group: Char, num: Int, value: Int): Int {
        val dataPath = composePinPath(group, num) + mDrvLevelName
        getPermission(dataPath)
        return nativeWriteGpio(dataPath, value.toString())
    }

    fun getDrvLevel(group: Char, num: Int): Int {
        val dataPath = composePinPath(group, num) + mDrvLevelName
        getPermission(dataPath)
        return nativeReadGpio(dataPath)
    }

    fun setMulSel(group: Char, num: Int, value: Int): Int {
        val dataPath = composePinPath(group, num) + mMulSelName
        getPermission(dataPath)
        return nativeWriteGpio(dataPath, value.toString())
    }

    fun getMulSel(group: Char, num: Int): Int {
        val dataPath = composePinPath(group, num) + mMulSelName
        getPermission(dataPath)
        return nativeReadGpio(dataPath)
    }

    private fun composePinPath(group: Char, num: Int): String {
        val groupstr = group.toString().uppercase(Locale.getDefault())
        val numstr = num.toString()
        return mPathstr + groupstr + numstr
    }

    private fun getPermission(path: String) {
        val file = File(path)
        if (!file.canRead() || !file.canWrite()) {
            try {
                val su: Process = Runtime.getRuntime().exec("/system/xbin/su")
                val cmd = "chmod 666 $path\nexit\n"
                su.outputStream.write(cmd.toByteArray())
                if (su.waitFor() != 0 || !file.canRead() || !file.canWrite()) {
                    throw SecurityException()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                throw SecurityException()
            }
        }
    }

    companion object {
        init {
            System.loadLibrary("gpio")
        }

        @JvmStatic
        val instance: Gpio by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            Gpio()
        }
    }
}