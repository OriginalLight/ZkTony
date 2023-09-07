package com.zktony.android.data.entities.internal.defaults

import com.zktony.android.data.entities.internal.Process

/**
 * @author 刘贺贺
 * @date 2023/9/1 15:30
 */
object ProcessDefaults {

    fun defaultBlocking() = Process(
        type = Process.BLOCKING,
        duration = 1.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        status = Process.UPCOMING
    )

    fun defaultPrimaryAntibody() = Process(
        type = Process.PRIMARY_ANTIBODY,
        duration = 12.0,
        temperature = 4.0,
        dosage = 8000.0,
        recycle = true,
        origin = 0,
        times = 0,
        status = Process.UPCOMING
    )

    fun defaultSecondaryAntibody() = Process(
        type = Process.SECONDARY_ANTIBODY,
        duration = 1.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        status = Process.UPCOMING
    )

    fun defaultWashing() = Process(
        type = Process.WASHING,
        duration = 5.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 3,
        status = Process.UPCOMING
    )

    fun defaultPhosphateBufferedSaline() = Process(
        type = Process.PHOSPHATE_BUFFERED_SALINE,
        duration = 0.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        status = Process.UPCOMING
    )
}