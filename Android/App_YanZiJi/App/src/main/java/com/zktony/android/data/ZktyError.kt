package com.zktony.android.data

enum class ZktyError(val code: Long, val message: String, val severity: Int) {
    ERROR_1(0x00000001, "系统参数错误", 1),
    ERROR_2(0x00000002, "管路中有废液体", 0),
    ERROR_3(0x00000004, "气泡传感器故障", 1),
    ERROR_4(0x00000008, "运行时光耦丢失", 0),
    ERROR_5(0x00000010, "运行时超温", 0),
    ERROR_6(0x00000020, "液路堵塞", 1),
    ERROR_7(0x00000040, "实验液体不足-转膜液", 0),
    ERROR_8(0x00000080, "清洗液体不足-清洗液", 0),
    ERROR_9(0x00000100, "实验控制超时", 0),
    ERROR_10(0x00000200, "排液超时-液路堵塞", 1),
    ERROR_11(0x00000400, "电压超负载", 1),
    ERROR_12(0x00000800, "电流超负载", 1),
    ERROR_13(0x00001000, "功率超负载", 1),
    ERROR_14(0x00002000, "短路/过载报警", 1),
    ERROR_15(0x00004000, "电压突变", 1),
    ERROR_16(0x00008000, "电流突变", 1),
    ERROR_17(0x00010000, "DAC芯片异常", 1);

    companion object {
        fun fromCode(code: Long): List<ZktyError> {
            val errors = mutableListOf<ZktyError>()
            entries.forEach {
                if (it.code and code != 0L) {
                    errors.add(it)
                }
            }
            return errors
        }

        fun fromCodeSignal(code: Long): ZktyError? {
            entries.forEach {
                if (it.code == code) {
                    return it
                }
            }
            return null
        }

        fun hasSeverity(code: Long, severity: Int): Boolean {
            return fromCode(code).any { it.severity == severity }
        }
    }
}