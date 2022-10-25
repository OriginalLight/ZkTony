package com.zktony.www.data.repository

import javax.inject.Inject

/**
 * @author: 刘贺贺
 * @date: 2022-10-25 14:26
 */
class RoomRepository @Inject constructor(
    private val motorRepository: MotorRepository,
    private val calibrationRepository: CalibrationRepository
) {
    fun getMotorRepository() = motorRepository
    fun getCailbrationRepository() = calibrationRepository
}