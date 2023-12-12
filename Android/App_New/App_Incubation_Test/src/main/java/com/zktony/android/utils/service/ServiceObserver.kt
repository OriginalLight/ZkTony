package com.zktony.android.utils.service

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject

class ServiceObserver @Inject constructor(
    private val s1: CalibrationService,
    private val s2: HistoryService
) : LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                s1.create()
                s2.create()

            }

            Lifecycle.Event.ON_DESTROY -> {
                s1.destroy()
                s2.destroy()
            }

            else -> {}
        }
    }
}