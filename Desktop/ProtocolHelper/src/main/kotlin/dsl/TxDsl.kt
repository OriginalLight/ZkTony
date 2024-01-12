package dsl

import com.zktony.android.core.utils.ControlType
import ext.writeFloatLE
import ext.writeInt32LE
import ext.writeInt8

/**
 * @author 刘贺贺
 * @date 2023/6/30 9:14
 */

/**
 * TxDsl
 *
 * @property byteList MutableList<Byte>
 * @constructor
 */
class TxDsl {
    val byteList: MutableList<Byte> = mutableListOf()
    var controlType: ControlType = ControlType.CONTROL_RESET

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
     * move with pulse
     *
     * @param block Function1<PM, Unit>
     * @return Unit
     */
    fun run(block: PM.() -> Unit) {
        controlType = ControlType.CONTROL_MOVE
        val pm = PM().apply(block)
        val ba = ByteArray(17)
        ba.writeInt8(pm.index, 0).writeInt32LE(pm.steps, 1).writeInt32LE(pm.acc, 5).writeInt32LE(pm.dec, 9)
            .writeInt32LE(pm.speed, 13)
        byteList.addAll(ba.toList())
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

class PM {
    var index: Int = 0
    var steps: Long = 0
    var acc: Long = 0
    var dec: Long = 0
    var speed: Long = 0
}