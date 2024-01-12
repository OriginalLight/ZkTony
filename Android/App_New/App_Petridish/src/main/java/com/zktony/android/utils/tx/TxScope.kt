package com.zktony.android.utils.tx

import com.zktony.android.data.entities.Motor
import com.zktony.android.utils.AppStateUtils
import com.zktony.android.utils.ext.dataSaver
import com.zktony.serialport.ext.writeInt32LE
import com.zktony.serialport.ext.writeInt8

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
class TxScope {
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
    fun move(type: MoveType = MoveType.MOVE_DV, block: MoveScope.() -> Unit) {
        controlType = ControlType.CONTROL_MOVE
        val moveScope = MoveScope().apply(block)

        val ads = moveScope.ads
        var adsBytes = ByteArray(12)
        if (ads == null) {
            val hpm = AppStateUtils.hpm[moveScope.index] ?: Motor()
            adsBytes.writeInt32LE(hpm.acc, 0).writeInt32LE(hpm.dec, 4).writeInt32LE(hpm.speed, 8)
        } else {
            adsBytes.writeInt32LE(ads.first, 0).writeInt32LE(ads.second, 4)
                .writeInt32LE(ads.third, 8)
        }

        when (type) {
            //按照校准数据运动
            MoveType.MOVE_DV -> {
                var jyh = 0f
                var jyq = 0f
                if (moveScope.index == 3) {
                    jyh = dataSaver.readData("jyh", 0f)
                    jyq = dataSaver.readData("jyq", 0f)
                } else {
                    jyh = dataSaver.readData("jyh2", 0f)
                    jyq = dataSaver.readData("jyq2", 0f)
                }
                val pulse = ((moveScope.dv / ((jyh - jyq) / 10 * 1000)) * 3200L).toLong()
                if (pulse != 0L) {
//                    if (moveScope.index != 3) {
//                        jyh = dataSaver.readData("jyh", 0f)
//                        jyq = dataSaver.readData("jyq", 0f)
//                        val pulse3 = ((moveScope.dv / ((jyh - jyq) / 10 * 1000)) * 3200L).toLong()
//
//                        val pulse_multiple = pulse3 / pulse
//                        val hpm3 = AppStateUtils.hpm[3] ?: Motor()
//                        val hpm8 = AppStateUtils.hpm[8] ?: Motor()
//                        val speed = hpm3.speed * pulse_multiple
//                        adsBytes.writeInt32LE(hpm8.acc, 0).writeInt32LE(hpm8.dec, 4)
//                            .writeInt32LE(speed, 8)
//                    }
                    val ba = ByteArray(5)
                    ba.writeInt8(moveScope.index, 0).writeInt32LE(pulse, 1)
                    byteList.addAll(ba.toList())
                    byteList.addAll(adsBytes.toList())
                    indexList.add(moveScope.index)
                }
            }
            //按照步数运动/3200
            MoveType.MOVE_PULSE -> {
                val pulse = pulse(moveScope.index, moveScope.pulse)
                if (pulse != 0L) {
                    val ba = ByteArray(5)
                    ba.writeInt8(moveScope.index, 0).writeInt32LE(pulse, 1)
                    byteList.addAll(ba.toList())
                    byteList.addAll(adsBytes.toList())
                    indexList.add(moveScope.index)
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

