package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.getGpio
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class SplashViewModel : ViewModel() {

    private val _loading = MutableStateFlow(0)


    init {
        viewModelScope.launch {
            launch {
//                initializer()
            }

        }
    }

    private fun initializer() {
        viewModelScope.launch {
            _loading.value = 1
            try {
                // Initialize the axes and syringe within a timeout of 60 seconds
                withTimeout(60 * 1000L) {
                    val ids = listOf(0, 1, 2, 3, 4)
                    // 查询GPIO状态
                    tx { queryGpio(ids) }
                    delay(300L)
                    // 针对每个电机进行初始化
                    ids.forEach {
                        // 如果电机未初始化，则进行初始化
                        if (!getGpio(it)) {
                            // 进行电机初始化
                            tx {
                                timeout = 1000L * 60
                                move(MoveType.MOVE_PULSE) {
                                    index = it
                                    pulse = 3200L * -30
                                    acc = 50
                                    dec = 80
                                    speed = 100
                                }
                            }
                        }

                        // 进行正向运动
                        tx {
                            timeout = 1000L * 10
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 3200L * 2
                                acc = 50
                                dec = 80
                                speed = 100
                            }
                        }

                        // 进行反向运动
                        tx {
                            timeout = 1000L * 15
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 3200L * -3
                                acc = 50
                                dec = 80
                                speed = 100
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                _loading.value = 0
            } finally {
                _loading.value = 0
            }
        }
    }

}
