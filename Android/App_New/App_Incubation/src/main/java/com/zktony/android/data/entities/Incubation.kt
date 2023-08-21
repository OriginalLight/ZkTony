package com.zktony.android.data.entities

/**
 * @author 刘贺贺
 * @date 2023/8/21 10:38
 */
sealed class IncubationFlow(
    open val tag: IncubationTag,
    open val displayText: String,
    open val duration: Double,
    open val temperature: Double,
    open val dosage: Double,
) {
    data class Blocking constructor(
        override val tag: IncubationTag = IncubationTag.BLOCKING,
        override val displayText: String = "封闭液",
        override val duration: Double = 1.0,
        override val temperature: Double = 37.0,
        override val dosage: Double = 8000.0,
    ) : IncubationFlow(tag, displayText, duration, temperature, dosage)

    data class PrimaryAntibody constructor(
        override val tag: IncubationTag = IncubationTag.PRIMARY_ANTIBODY,
        override val displayText: String = "一抗",
        override val duration: Double = 12.0,
        override val temperature: Double = 4.0,
        override val dosage: Double = 5000.0,
        val recycle: Boolean = true,
    ) : IncubationFlow(tag, displayText, duration, temperature, dosage)

    data class SecondaryAntibody constructor(
        override val tag: IncubationTag = IncubationTag.SECONDARY_ANTIBODY,
        override val displayText: String = "二抗",
        override val duration: Double = 1.0,
        override val temperature: Double = 37.0,
        override val dosage: Double = 8000.0,
    ) : IncubationFlow(tag, displayText, duration, temperature, dosage)

    data class Washing constructor(
        override val tag: IncubationTag = IncubationTag.WASHING,
        override val displayText: String = "洗涤",
        override val duration: Double = 5.0,
        override val temperature: Double = 37.0,
        override val dosage: Double = 8000.0,
        val times: Int = 1,
    ) : IncubationFlow(tag, displayText, duration, temperature, dosage)

    data class PhosphateBufferedSaline constructor(
        override val tag: IncubationTag = IncubationTag.PHOSPHATE_BUFFERED_SALINE,
        override val displayText: String = "缓冲液",
        override val duration: Double = 0.0,
        override val temperature: Double = 4.0,
        override val dosage: Double = 8000.0,
    ) : IncubationFlow(tag, displayText, duration, temperature, dosage)
}

enum class IncubationTag {
    BLOCKING,
    PRIMARY_ANTIBODY,
    SECONDARY_ANTIBODY,
    WASHING,
    PHOSPHATE_BUFFERED_SALINE
}
