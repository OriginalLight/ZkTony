package com.zktony.android.data.entities.internal.defaults

import com.zktony.android.data.entities.internal.IncubationStage
import java.util.UUID

/**
 * @author 刘贺贺
 * @date 2023/9/1 15:30
 */
object StageDefaults {

    fun defaultBlocking() = IncubationStage(
        uuid = UUID.randomUUID().toString(),
        type = 0,
        duration = 1.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        flags = 2
    )

    fun defaultPrimaryAntibody() = IncubationStage(
        uuid = UUID.randomUUID().toString(),
        type = 1,
        duration = 12.0,
        temperature = 4.0,
        dosage = 8000.0,
        recycle = true,
        origin = 0,
        times = 0,
        flags = 2
    )

    fun defaultSecondaryAntibody() = IncubationStage(
        uuid = UUID.randomUUID().toString(),
        type = 2,
        duration = 1.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        flags = 2
    )

    fun defaultWashing() = IncubationStage(
        uuid = UUID.randomUUID().toString(),
        type = 3,
        duration = 5.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 3,
        flags = 2
    )

    fun defaultPhosphateBufferedSaline() = IncubationStage(
        uuid = UUID.randomUUID().toString(),
        type = 4,
        duration = 0.0,
        temperature = 4.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        flags = 2
    )

    fun defaultByType(type: Int) = when (type) {
        0 -> defaultBlocking()
        1 -> defaultPrimaryAntibody()
        2 -> defaultSecondaryAntibody()
        3 -> defaultWashing()
        4 -> defaultPhosphateBufferedSaline()
        else -> defaultPrimaryAntibody()
    }
}