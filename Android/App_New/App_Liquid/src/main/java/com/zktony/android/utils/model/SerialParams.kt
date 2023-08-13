package com.zktony.android.utils.model

import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.ext.pulse
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8

class SerialParams {
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
     * move
     *
     * @param type MoveType
     * @param block [@kotlin.ExtensionFunctionType] Function1<MoveScope, Unit>
     * @return Unit
     */
    fun move(type: MoveType = MoveType.MOVE_DV, block: MoveParams.() -> Unit) {
        controlType = ControlType.CONTROL_MOVE
        val scope = MoveParams().apply(block)
        when (type) {
            MoveType.MOVE_DV -> {
                val pulse = pulse(scope.index, scope.dv)
                val config =
                    Motor(speed = scope.speed, acc = scope.acc, dec = scope.dec)
                if (pulse != 0L) {
                    val ba = ByteArray(5)
                    ba.writeInt8(scope.index, 0).writeInt32LE(pulse, 1)
                    byteList.addAll(ba.toList())
                    byteList.addAll(config.toByteArray().toList())
                    indexList.add(scope.index)
                }
            }

            MoveType.MOVE_PULSE -> {
                val pulse = pulse(scope.index, scope.pulse)
                val config =
                    Motor(speed = scope.speed, acc = scope.acc, dec = scope.dec)
                if (pulse != 0L) {
                    val ba = ByteArray(5)
                    ba.writeInt8(scope.index, 0).writeInt32LE(pulse, 1)
                    byteList.addAll(ba.toList())
                    byteList.addAll(config.toByteArray().toList())
                    indexList.add(scope.index)
                }
            }
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
}