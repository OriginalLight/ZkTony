package com.zktony.android.utils.service

import javax.inject.Inject

class ServiceFactory @Inject constructor(
    private val curveService: CurveService,
    private val historyService: HistoryService
) {
    fun getCurveService() = curveService
    fun getHistoryService() = historyService

    fun create() {
        curveService.start()
        historyService.start()
    }
}