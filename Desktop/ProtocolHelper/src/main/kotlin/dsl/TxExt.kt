package dsl

import com.zktony.android.core.utils.ControlType
import protocol

/**
 * 发送命令
 *
 * @param block [TxDsl.() -> Unit] 命令构建器
 * @return [Unit]
 */
fun tx(block: TxDsl.() -> Unit): ByteArray {
    // 构建命令
    val tx = TxDsl().apply(block)

    // 根据控制类型执行相应的操作
    when (tx.controlType) {
        // 复位
        ControlType.CONTROL_RESET -> {
            return protocol {
                control = 0x00
                data = tx.byteList.toByteArray()
            }.toByteArray()
        }

        // 运动
        ControlType.CONTROL_MOVE -> {
            return protocol {
                control = 0x01
                data = tx.byteList.toByteArray()
            }.toByteArray()
        }

        // 停止
        ControlType.CONTROL_STOP -> {
            return protocol {
                control = 0x02
                data = tx.byteList.toByteArray()
            }.toByteArray()
        }

        // 查询轴状态
        ControlType.CONTROL_QUERY_AXIS -> {
            return protocol {
                control = 0x03
                data = tx.byteList.toByteArray()
            }.toByteArray()
        }

        // 查询GPIO状态
        ControlType.CONTROL_QUERY_GPIO -> {
            return protocol {
                control = 0x04
                data = tx.byteList.toByteArray()
            }.toByteArray()
        }

        // 控制气阀
        ControlType.CONTROL_VALVE -> {
            return protocol {
                control = 0x05
                data = tx.byteList.toByteArray()
            }.toByteArray()
        }
    }
}