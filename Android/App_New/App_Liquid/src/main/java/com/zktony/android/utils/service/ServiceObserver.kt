package com.zktony.android.utils.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject

class ServiceObserver @Inject constructor(
    private val calibrationService: CalibrationService,
    private val motorService: MotorService
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                calibrationService.create()
                motorService.create()
            }

            Lifecycle.Event.ON_DESTROY -> {
                calibrationService.destroy()
                motorService.destroy()
            }

            else -> {}
        }
    }
}