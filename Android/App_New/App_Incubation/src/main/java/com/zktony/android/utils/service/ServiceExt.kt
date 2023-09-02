package com.zktony.android.utils.service

import org.koin.java.KoinJavaComponent.inject

val curveService: CurveService by inject(CurveService::class.java)

val historyService: HistoryService by inject(HistoryService::class.java)

fun setupServices() {
    curveService.setup()
    historyService.setup()
}