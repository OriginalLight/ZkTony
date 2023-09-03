package com.zktony.android.utils.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject

class ServiceObserver @Inject constructor(
    private val curveService: CurveService,
    private val historyService: HistoryService
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_DESTROY -> {
                curveService.takeIf { event == Lifecycle.Event.ON_CREATE }?.create()
                historyService.takeIf { event == Lifecycle.Event.ON_CREATE }?.create()
                curveService.takeIf { event == Lifecycle.Event.ON_DESTROY }?.destroy()
                historyService.takeIf { event == Lifecycle.Event.ON_DESTROY }?.destroy()
            }

            else -> {
            }
        }
    }
}