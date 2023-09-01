package com.zktony.android.data.entities.internal.defaults

import com.zktony.android.data.entities.internal.Process
import com.zktony.android.data.entities.internal.ProcessStatus
import com.zktony.android.data.entities.internal.ProcessType

/**
 * @author 刘贺贺
 * @date 2023/9/1 15:30
 */
object ProcessDefaults {

    fun defaultBlocking() = Process(
        type = ProcessType.BLOCKING,
        duration = 1.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        status = ProcessStatus.UPCOMING
    )

    fun defaultPrimaryAntibody() = Process(
        type = ProcessType.PRIMARY_ANTIBODY,
        duration = 12.0,
        temperature = 4.0,
        dosage = 8000.0,
        recycle = true,
        origin = 0,
        times = 0,
        status = ProcessStatus.UPCOMING
    )

    fun defaultSecondaryAntibody() = Process(
        type = ProcessType.SECONDARY_ANTIBODY,
        duration = 1.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        status = ProcessStatus.UPCOMING
    )

    fun defaultWashing() = Process(
        type = ProcessType.WASHING,
        duration = 5.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 3,
        status = ProcessStatus.UPCOMING
    )

    fun defaultPhosphateBufferedSaline() = Process(
        type = ProcessType.PHOSPHATE_BUFFERED_SALINE,
        duration = 0.0,
        temperature = 37.0,
        dosage = 8000.0,
        recycle = false,
        origin = 0,
        times = 0,
        status = ProcessStatus.UPCOMING
    )
}