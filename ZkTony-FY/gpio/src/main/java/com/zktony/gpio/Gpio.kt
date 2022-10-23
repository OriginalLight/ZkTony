@file:Suppress("unused")
package com.zktony.gpio

import com.zktony.gpio.utils.Logger
import java.io.File

/**
 * @author 刘贺贺
 */
class Gpio {
    private val mPathstr = "/sys/class/gpio/"
    private val mDirection = "/direction"
    private val mValue = "/value"

    /**
     * 写入Gpio
     * @param path [String] 路径
     * @param value [String] 写入的值
     * @return [Int] 是否写入成功
     */
    external fun nativeWriteGpio(path: String, value: String): Int

    /**
     * 读取Gpio
     * @param path [String] 路径
     * @return [Int] 读取的值
     */
    external fun nativeReadGpio(path: String): Int

    /**
     * 写入Gpio
     * @param gpio [String] Gpio
     * @param value [Int] 写入的值
     * @return [Int] 是否写入成功
     */
    fun writeGpio(gpio: String, value: Int): Int {
        val dataPath = composePinPath(gpio) + mValue
        Logger.instance.i(TAG, "writeGpio: $dataPath, value: $value")
        getPermission(dataPath)
        return nativeWriteGpio(dataPath, value.toString())
    }

    /**
     * 读取Gpio
     * @param gpio [String] Gpio
     * @return [Int] 读取的值
     */
    fun readGpio(gpio: String): Int {
        val dataPath = composePinPath(gpio) + mValue
        Logger.instance.i(TAG, "readGpio: $dataPath")
        getPermission(dataPath)
        return nativeReadGpio(dataPath)
    }

    /**
     * 设置Gpio方向
     * @param gpio [String] Gpio
     * @param value [Int] 方向
     * @return [Int] 是否设置成功
     */
    fun setDirection(gpio: String, value: Int): Int {
        val dataPath = composePinPath(gpio) + mDirection
        Logger.instance.i(TAG, "setDirection: $dataPath, value: $value")
        getPermission(dataPath)
        return nativeWriteGpio(dataPath, value.toString())
    }

    /**
     * 获取Gpio方向
     * @param gpio [String] Gpio
     * @return [Int] 方向
     */
    fun getDirection(gpio: String): Int {
        val dataPath = composePinPath(gpio) + mDirection
        Logger.instance.i(TAG, "getDirection: $dataPath")
        getPermission(dataPath)
        return nativeReadGpio(dataPath)
    }

    /**
     * 组合Gpio路径
     * @param gpio [String] Gpio
     */
    private fun composePinPath(gpio: String): String {
        return mPathstr + gpio
    }

    /**
     * 获取权限
     * @param path [String] 路径
     */
    private fun getPermission(path: String) {
        val file = File(path)
        Logger.instance.i(TAG, "检测读写权限: 是否可读:${file.canRead()}，是否可写：${file.canWrite()}")
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

        private const val TAG = "Gpio"
    }
}