package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.ext.dataSaver
import com.zktony.android.utils.tx.ExecuteType
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.getGpio
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * @author: 刘贺贺
 * @date: 2023-02-14 15:37
 */
class HomeViewModel constructor(private val dao: ProgramDao) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.LIST)
    private val _loading = MutableStateFlow(0)

    /**
     * 计数
     */
    private val _count = MutableStateFlow(0)

    private val _job = MutableStateFlow<Job?>(null)
    private var syringeJob: Job? = null


    val uiState = _uiState.asStateFlow()

    val count = _count.asStateFlow()


    /**
     * 举升1复位高度
     */
    val fwgd = dataSaver.readData("fwgd", 0f);


    /**
     * 举升1盘子距离
     */
    val pzgd = dataSaver.readData("pzjl", 0f);


    /**
     * 举升1夹爪高度
     */
    val jzgd = dataSaver.readData("jzgd", 0f);

    /**
     * 举升1分离距离
     */
    val fljl = dataSaver.readData("fljl", 0f);

    /**
     * 举升1矫正高度
     */
    val jiaozgd = dataSaver.readData("jiaozgd", 0f);

    /**
     * 举升1下盘高度
     */
    val xpgd = dataSaver.readData("xpgd", 0f);


    /**
     * 举升2复位高度
     */
    val fwgd2 = dataSaver.readData("fwgd2", 0f);

    /**
     * 举升2盘子距离
     */
    val pzgd2 = dataSaver.readData("pzjl2", 0f);

    /**
     * 夹紧距离
     */
    val jjjl = dataSaver.readData("jjjl", 0f);

    /**
     * 松开距离
     */
    val skjl = dataSaver.readData("skjl", 0f);

    /**
     * 上盘原点距离
     */
    val spydjl = dataSaver.readData("spydjl", 0f);

    /**
     * 上盘孔位距离1
     */
    val spkwjl1 = dataSaver.readData("spkwjl1", 0f);

    /**
     * 上盘孔位距离2
     */
    val spkwjl2 = dataSaver.readData("spkwjl1", 0f);

    /**
     * 上盘孔位距离3
     */
    val spkwjl3 = dataSaver.readData("spkwjl1", 0f);

    /**
     * 上盘孔位距离4
     */
    val spkwjl4 = dataSaver.readData("spkwjl1", 0f);

    /**
     * 上盘孔位距离5
     */
    val spkwjl5 = dataSaver.readData("spkwjl1", 0f);

    /**
     * 上盘孔位距离6
     */
    val spkwjl6 = dataSaver.readData("spkwjl1", 0f);

    /**
     * 上盘孔位距离7
     */
    val spkwjl7 = dataSaver.readData("spkwjl1", 0f);

    /**
     * 上盘孔位距离8
     */
    val spkwjl8 = dataSaver.readData("spkwjl1", 0f);


    /**
     * 下盘原点距离
     */
    val xpydjl = dataSaver.readData("xpydjl", 0f);

    /**
     * 下盘孔位距离1
     */
    val xpkwjl1 = dataSaver.readData("xpkwjl", 0f);

    /**
     * 下盘孔位距离2
     */
    val xpkwjl2 = dataSaver.readData("xpkwjl1", 0f);

    /**
     * 下盘孔位距离3
     */
    val xpkwjl3 = dataSaver.readData("xpkwjl1", 0f);


    init {
        viewModelScope.launch {
            combine(
                dao.getAll(), // Step 1: Observe changes in the database by calling the getAll function
                _selected,
                _page,
                _loading,
                _job,
            ) { entities, selected, page, loading, job ->
                HomeUiState(
                    entities = entities,
                    selected = selected,
                    page = page,
                    loading = loading,
                    job = job,
                )
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it // Step 2: Update the UI state with the new values
            }
        }
    }

    /**
     * Handles the given Home screen event.
     *
     * @param event The Home screen event to handle.
     */
    fun event(event: HomeEvent) {
        when (event) {
            is HomeEvent.Reset -> reset()
            is HomeEvent.Start -> start(event.index)
            is HomeEvent.Stop -> stop()
            is HomeEvent.NavTo -> _page.value = event.page
            is HomeEvent.ToggleSelected -> _selected.value = event.id
            is HomeEvent.Clean -> clean(event.index)
            is HomeEvent.Syringe -> syringe(event.index)
            is HomeEvent.Pipeline -> pipeline(event.index)
            else -> {}
        }
    }

    /**
     * Resets the Home screen by initializing the axes and syringe.
     */
    private fun reset() {
        viewModelScope.launch {
            _loading.value = 1
            try {
                // Initialize the axes and syringe within a timeout of 60 seconds
                withTimeout(60 * 1000L) {


                    val ids = listOf(1, 0, 2, 4, 5)
                    // 查询GPIO状态
                    tx {
                        queryGpio(ids)
                        delay = 300L
                    }
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

                    //移动上盘到原点距离
                    tx {
                        timeout = 1000L * 60
                        move(MoveType.MOVE_PULSE) {
                            index = 5
                            pulse = (3200L * spydjl).toLong();
                            speed = 100
                        }

                    }
                    //移动下盘到原点距离
                    tx {
                        timeout = 1000L * 60
                        move(MoveType.MOVE_PULSE) {
                            index = 4
                            pulse = (3200L * xpydjl).toLong();
                            speed = 100
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


//    private fun start(index: Int) {
//        viewModelScope.launch {
//            _loading.value = 7
//            while (true) {
//                _count.value += 1
//                delay(100L)
//            }
//
//        }
//    }

    /**
     * Starts the execution of the selected program entity.
     */
    private fun start(int: Int) {
        viewModelScope.launch {
            _loading.value = 7
            /**
             * 是否有培养皿运动过
             */
            var isStart = false

            /**
             * 上盘运动次数
             */
            var spStartNum = 0

            /**
             * 下盘移动坐标
             * 从1开始
             */
            var xpStartCoordinates = 1;

            /**
             * 上盘移动坐标
             * 从1开始
             */
            var spStartCoordinates = 1;

            /**
             * 运动状态
             * 0=运动的准备工作
             * 1=有培养皿的运动
             * 2=没有培养皿的运动
             * 3=培养皿摆放正确
             * 4=培养皿摆放错误
             * 5=结束运动的最后几个动作
             */
            var startState = 0;


            /**
             *  举升2移动到举升盘子高度，防止开机前有培养皿
             */
            tx {
                move(MoveType.MOVE_PULSE) {
                    index = 0
                    pulse = (3200L * pzgd2).toLong();
                }
            }

            /**
             * 1.判断是否是第一次运动
             * 2.检测是否有培养皿
             * 3.检测培养皿摆放状态
             */
            while (true) {
                if (startState == 0) {

                    /**
                     *  1.举升1移动到举升夹爪高度
                     *  2.举升2移动到0
                     *  3.夹爪松开
                     */
                    tx {
                        //1.举升1移动到举升夹爪高度
                        move(MoveType.MOVE_PULSE) {
                            index = 1
                            pulse = (3200L * jzgd).toLong()
                        }
                        //2.举升2移动到0
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = 0
                        }
                        //3.夹爪松开
                        move(MoveType.MOVE_PULSE) {
                            index = 2
                            pulse = (3200L * skjl).toLong();
                        }

                        queryGpio(6)
                        delay = 300L
                    }

                    /**
                     * 获取是否有培养皿
                     * false=有
                     * true=没有
                     */
                    val jiance1 = getGpio(6)
                    if (!jiance1) {
                        /**
                         * 有培养皿的运动
                         */
                        startState = 1;
                        continue;
                    } else {
                        /**
                         * 没有有培养皿的运动
                         */
                        startState = 2;
                        continue;
                    }
                } else if (startState == 1) {
                    /**
                     * 有培养皿的运动
                     */
                    /**
                     * 第一次有培养皿的运动
                     */
                    if (!isStart) {
                        tx {
                            //1.举升1移动到分离距离
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = (3200L * fljl).toLong()
                            }
                            //2.夹爪夹紧
                            move(MoveType.MOVE_PULSE) {
                                index = 2
                                pulse = (3200L * jjjl).toLong()
                            }
                            //3.举升1移动到0
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = 0
                            }
                            //4.举升2移动到0
                            move(MoveType.MOVE_PULSE) {
                                index = 0
                                pulse = 0
                            }

                            /**
                             * 检测培养皿是否摆放正确
                             */
                            queryGpio(7)
                            delay = 300L
                        }


                        /**
                         * 获取检测培养皿是否摆放正确
                         * false=没有
                         * true=有
                         */
                        val jiance2 = getGpio(7)

                        if (jiance2) {
                            startState = 3;
                            continue;
                        } else {
                            startState = 4;
                            continue;
                        }
                    }


                } else if (startState == 2) {
                    /**
                     * 检测没有有培养皿的运动
                     */

                    //第一次运动
                    if (!isStart) {
                        /**
                         * 打开夹爪（1号顶升机构顶升至上料台面齐平位置，培养皿上料机构旋转至第二培养皿放置位置。开始执行第二步骤。）
                         */
                        tx {
                            //举升1运动到复位高度
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = (3200L * fwgd).toLong();
                            }

                            //夹爪松开
                            move(MoveType.MOVE_PULSE) {
                                index = 2
                                pulse = (3200L * skjl).toLong();
                            }

                            //1.上盘移动1格
                            /**
                             * 上盘孔位距离
                             */
                            val spkwjl = dataSaver.readData("spkwjl" + spStartCoordinates, 0f);

                            move(MoveType.MOVE_PULSE) {
                                index = 5
                                pulse = (3200L * spkwjl).toLong();
                            }
                        }
                        if (spStartCoordinates < 8) {
                            spStartCoordinates += 1;
                        } else {
                            spStartCoordinates = 1;
                        }
                        startState = 0;
                        continue;


                    } else {
                        //不是第一次运动

                        tx {
                            //1.举升1移动到0
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = 0
                            }
                            //2.举升2移动到0
                            move(MoveType.MOVE_PULSE) {
                                index = 0
                                pulse = (3200L * fwgd2).toLong();
                            }

                            //3.夹爪松开
                            move(MoveType.MOVE_PULSE) {
                                index = 2
                                pulse = (3200L * skjl).toLong();
                            }

                            //2.举升2移动到0
                            move(MoveType.MOVE_PULSE) {
                                index = 0
                                pulse = 0
                            }
                            var i = 0;
                            while (i < 3) {
                                /**
                                 * 下盘孔位距离
                                 */
                                val xpkwjl = dataSaver.readData("xpkwjl" + xpStartCoordinates, 0f);
                                /**
                                 * 下盘移动一格
                                 */
                                move(MoveType.MOVE_PULSE) {
                                    index = 4
                                    pulse = (3200L * xpkwjl).toLong();
                                }

                                /**
                                 * 举升2举升到复位高度
                                 */
                                move(MoveType.MOVE_PULSE) {
                                    index = 0
                                    pulse = (3200L * fwgd2).toLong();
                                }

                                if (xpStartCoordinates < 3) {
                                    xpStartCoordinates += 1;
                                } else {
                                    xpStartCoordinates = 1;

                                    /**
                                     * 下盘回到原点距离
                                     */
                                    move(MoveType.MOVE_PULSE) {
                                        index = 4
                                        pulse = (3200L * xpydjl).toLong();
                                    }
                                }
                            }

                            if (spStartCoordinates < 8) {
                                spStartCoordinates += 1;
                                //1.上盘移动1格
                                /**
                                 * 上盘孔位距离
                                 */
                                val spkwjl = dataSaver.readData("spkwjl" + spStartCoordinates, 0f);

                                move(MoveType.MOVE_PULSE) {
                                    index = 5
                                    pulse = (3200L * spkwjl).toLong();
                                }
                            }


                        }

                        if (spStartCoordinates == 8) {
                            _loading.value = 0
                            //TODO 运动结束
                            break;
                        }
                        startState = 0;

                        if (int == 8) {
                            _loading.value = 0
                            break;
                        }

                        continue;
                    }


                } else if (startState == 3) {
                    /**
                     * 培养皿摆放正确的运动
                     */

                    //1.下盘移动1格
                    /**
                     * 下盘孔位距离
                     */
                    val xpkwjl = dataSaver.readData("xpkwjl" + xpStartCoordinates, 0f);
                    tx {
                        /**
                         * 下盘移动一格
                         */
                        move(MoveType.MOVE_PULSE) {
                            index = 4
                            pulse = (3200L * xpkwjl).toLong();
                        }

                        /**
                         * 加液
                         */
                        //TODO 暂时加液
                        move(MoveType.MOVE_PULSE) {
                            index = 8
                            pulse = 3200L * 10;
                        }

                        /**
                         * 举升1举升到夹爪高度
                         */
                        move(MoveType.MOVE_PULSE) {
                            index = 1
                            pulse = (3200L * jzgd).toLong();
                        }

                        /**
                         * 举升2举升到复位高度
                         */
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = (3200L * fwgd2).toLong();
                        }

                        if (xpStartCoordinates < 3) {
                            xpStartCoordinates += 1;
                        } else {
                            xpStartCoordinates = 1;

                            /**
                             * 下盘回到原点距离
                             */
                            move(MoveType.MOVE_PULSE) {
                                index = 4
                                pulse = (3200L * xpydjl).toLong();
                            }
                        }

                    }

                    _count.value += 1;
                    startState = 0;
                    continue;


                } else if (startState == 4) {
                    /**
                     * 培养皿摆放错误的运动
                     */

                    var jiance2 = false;
                    /**
                     * 培养皿摆放不正确的次数，3次就停止运动
                     */
                    var displayError = 0;
                    while (displayError < 3) {
                        tx {
                            /**
                             * 举升1举升到矫正高度
                             */
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = (3200L * jiaozgd).toLong();
                            }

                            /**
                             * 举升1举升到0
                             */
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = 0;
                            }

                            /**
                             * 举升2举升到0
                             */
                            move(MoveType.MOVE_PULSE) {
                                index = 0
                                pulse = 0;
                            }

                            /**
                             * 检测培养皿是否摆放正确
                             */
                            queryGpio(7)
                            delay = 300L
                            /**
                             * 获取检测培养皿是否摆放正确
                             * false=没有
                             * true=有
                             */
                            jiance2 = getGpio(7)
                        }

                        if (jiance2) {
                            startState = 3;
                            continue;
                        }

                    }
                    _loading.value = 0
                    //TODO 运动结束
                    break;
                }
            }
        }
    }

    /**
     * Stops the execution of the Home screen.
     */
    private fun stop() {
        viewModelScope.launch {
            // Cancel and join the current job
            _job.value?.cancelAndJoin()
            _job.value = null

            // Reset the screen and stop the motors
            _loading.value = 1
            tx {
                delay = 500L
                reset()
            }
            tx {
                move {
                    index = 1
                    dv = 0f
                }
            }
            tx {
                move {
                    index = 0
                    dv = 0f
                }
            }

            // Close the syringe valve
            tx {
                delay = 100L
                valve(2 to 0)
            }

            // Perform a syringe operation to clear the system
            tx {
                timeout = 1000L * 60
                move(MoveType.MOVE_PULSE) {
                    index = 2
                    pulse = 0
                }
            }

            // Set the loading state to 0 to indicate that the execution has stopped
            _loading.value = 0
        }
    }


    /**
     * Performs a clean operation on a program entity.
     *
     * @param index The index of the program entity to perform the operation on.
     */
    private fun clean(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                // Stop all clean operations
                _loading.value = 0
                tx { stop(9) }
            } else {
                // Start a clean operation on the selected program entity
                _loading.value = 2
                tx {
                    executeType = ExecuteType.ASYNC
                    move(MoveType.MOVE_PULSE) {
                        this.index = 9
                        pulse = 3200L * 10000L
                    }
                }
            }
        }
    }

    /**
     * Performs a syringe operation on a program entity.
     *
     * @param index The index of the program entity to perform the operation on.
     */
    private fun syringe(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                // Stop all syringe operations
                _loading.value = 0
                syringeJob?.cancelAndJoin()
                syringeJob = null
                tx {
                    timeout = 1000L * 60
                    move(MoveType.MOVE_PULSE) {
                        this.index = 2
                        pulse = 0
                    }
                }
            } else {
                // Start a syringe operation on the selected program entity
                _loading.value = 3
                syringeJob = launch {
                    while (true) {
                        tx {
                            delay = 100L
                            valve(2 to if (index == 1) 1 else 0)
                        }
                        tx {
                            timeout = 1000L * 60
                            move(MoveType.MOVE_PULSE) {
                                this.index = 2
                                pulse = 0
                            }
                        }
                        tx {
                            delay = 100L
                            valve(2 to if (index == 1) 0 else 1)
                        }
                        tx {
                            timeout = 1000L * 60
                            move(MoveType.MOVE_PULSE) {
                                this.index = 2
                                pulse = 0
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Performs a pipeline operation on a program entity.
     *
     * @param index The index of the program entity to perform the operation on.
     */
    private fun pipeline(index: Int) {
        viewModelScope.launch {
            if (index == 0) {
                // Stop all pipeline operations
                _loading.value = 0
                tx { stop(3, 4, 5, 6, 7, 8) }
            } else {
                // Start a pipeline operation on the selected program entity
                _loading.value = 4
                tx {
                    executeType = ExecuteType.ASYNC
                    repeat(6) {
                        move(MoveType.MOVE_PULSE) {
                            this.index = it + 3
                            pulse = 3200L * 10000L * if (index == 1) 1 else -1
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for the UI state of the Home screen.
 *
 * @param entities The list of program entities to display.
 * @param selected The ID of the selected program entity.
 * @param page The current page type.
 * @param loading The loading state of the screen.
 * @param job The current execution job.
 */
data class HomeUiState(
    val entities: List<Program> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.LIST,
    /*
     * 0: loading closed
     * 1: resting
     * 2: clean
     * 3: syringe 1
     * 4: syringe 2
     * 5: pipeline 1
     * 6: pipeline 2
     * 7:start
     * 8:stop
     * 9:上板
     */
    val loading: Int = 0,
    val job: Job? = null,
    val count: Int = 0,
)

/**
 * Sealed class for the events of the Home screen.
 */
sealed class HomeEvent {
    data object Reset : HomeEvent()
    data class Start(val index: Int) : HomeEvent()
    data object Stop : HomeEvent()
    data class NavTo(val page: PageType) : HomeEvent()
    data class ToggleSelected(val id: Long) : HomeEvent()
    data class Clean(val index: Int) : HomeEvent()
    data class Syringe(val index: Int) : HomeEvent()
    data class Pipeline(val index: Int) : HomeEvent()
}