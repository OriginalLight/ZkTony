package com.zktony.android.utils.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject

class ServiceObserver @Inject constructor(
    private val calibrationService: CalibrationService,
    private val historyService: HistoryService
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                calibrationService.create()
                historyService.create()

            }

            Lifecycle.Event.ON_DESTROY -> {
                calibrationService.destroy()
                historyService.destroy()
            }

            else -> {}
        }
    }
}