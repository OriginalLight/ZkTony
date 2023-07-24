package com.zktony.android.ext.dsl

import com.zktony.android.data.entities.MotorEntity
import com.zktony.android.ext.utils.ControlType
import com.zktony.android.ext.utils.ExceptionPolicy
import com.zktony.android.ext.utils.ExecuteType
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8

/**
 * @author 刘贺贺
 * @date 2023/6/30 9:14
 */

/**
 * TxDsl
 *
 * @property byteList MutableList<Byte>
 * @property indexList MutableList<Int>
 * @property executeType ExecuteType
 * @property exceptionPolicy ExceptionPolicy
 * @property controlType ControlType
 * @property timeout Long
 * @property delay Long
 * @constructor
 */
class TxDsl {
    val byteList: MutableList<Byte> = mutableListOf()
    val indexList: MutableList<Int> = mutableListOf()
    var executeType: ExecuteType = ExecuteType.SYNC
    var exceptionPolicy: ExceptionPolicy = ExceptionPolicy.SKIP
    var controlType: ControlType = ControlType.CONTROL_RESET
    var timeout: Long = 1000L * 10
    var delay: Long = 0L

    /**
     * reset
     *
     * @return Unit
     */
    fun reset() {
        controlType = ControlType.CONTROL_RESET
        byteList.add(0x00)
    }

    /**
     * move with distance or volume
     *
     * @param block Function1<DM, Unit>
     * @return Unit
     */
    fun mdm(block: DM.() -> Unit) {
        controlType = ControlType.CONTROL_MOVE
        val dm = DM().apply(block)
        val pulse = pulse(dm.index, dm.dv)
        val config = MotorEntity(speed = dm.speed, acc = dm.acc, dec = dm.dec)
        if (pulse != 0L) {
            val ba = ByteArray(5)
            ba.writeInt8(dm.index, 0).writeInt32LE(pulse, 1)
            byteList.addAll(ba.toList())
            byteList.addAll(config.toByteArray().toList())
            indexList.add(dm.index)
        }
    }

    /**
     * move with pulse
     *
     * @param block Function1<PM, Unit>
     * @return Unit
     */
    fun mpm(block: PM.() -> Unit) {
        controlType = ControlType.CONTROL_MOVE
        val pm = PM().apply(block)
        val pulse = pulse(pm.index, pm.pulse)
        val config = MotorEntity(speed = pm.speed, acc = pm.acc, dec = pm.dec)
        if (pulse != 0L) {
            val ba = ByteArray(5)
            ba.writeInt8(pm.index, 0).writeInt32LE(pulse, 1)
            byteList.addAll(ba.toList())
            byteList.addAll(config.toByteArray().toList())
            indexList.add(pm.index)
        }
    }

    /**
     * stop
     *
     * @param ids List<Int>
     * @return Unit
     */
    fun stop(ids: List<Int>) {
        controlType = ControlType.CONTROL_STOP
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    /**
     * stop
     *
     * @param ids IntArray
     * @return Unit
     */
    fun stop(vararg ids: Int) {
        controlType = ControlType.CONTROL_STOP
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    /**
     * query axis status
     *
     * @param ids List<Int>
     * @return Unit
     */
    fun queryAxis(ids: List<Int>) {
        controlType = ControlType.CONTROL_QUERY_AXIS
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    /**
     * query axis status
     *
     * @param ids IntArray
     * @return Unit
     */
    fun queryAxis(vararg ids: Int) {
        controlType = ControlType.CONTROL_QUERY_AXIS
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    /**
     * query gpio status
     *
     * @param ids List<Int>
     * @return Unit
     */
    fun queryGpio(ids: List<Int>) {
        controlType = ControlType.CONTROL_QUERY_GPIO
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    /**
     * query gpio status
     *
     * @param ids IntArray
     * @return Unit
     */
    fun queryGpio(vararg ids: Int) {
        controlType = ControlType.CONTROL_QUERY_GPIO
        val byteArray = ByteArray(ids.size)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i, index)
        }
        byteList.addAll(byteArray.toList())
    }

    /**
     * set valve status
     *
     * @param ids List<Pair<Int, Int>>
     * @return Unit
     */
    fun valve(ids: List<Pair<Int, Int>>) {
        controlType = ControlType.CONTROL_VALVE
        val byteArray = ByteArray(ids.size * 2)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i.first, index * 2)
            byteArray.writeInt8(i.second, index * 2 + 1)
        }
        byteList.addAll(byteArray.toList())
    }

    /**
     * set valve status
     *
     * @param ids Array<out Pair<Int, Int>>
     * @return Unit
     */
    fun valve(vararg ids: Pair<Int, Int>) {
        controlType = ControlType.CONTROL_VALVE
        val byteArray = ByteArray(ids.size * 2)
        ids.forEachIndexed { index, i ->
            byteArray.writeInt8(i.first, index * 2)
            byteArray.writeInt8(i.second, index * 2 + 1)
        }
        byteList.addAll(byteArray.toList())
    }
}

class DM {
    var index: Int = 0
    var dv: Float = 0f
    var acc: Float = scheduleTask.hpm[index]!!.acc
    var dec: Float = scheduleTask.hpm[index]!!.dec
    var speed: Float = scheduleTask.hpm[index]!!.speed
}

class PM {
    var index: Int = 0
    var pulse: Long = 0
    var acc: Float = scheduleTask.hpm[index]!!.acc
    var dec: Float = scheduleTask.hpm[index]!!.dec
    var speed: Float = scheduleTask.hpm[index]!!.speed
}