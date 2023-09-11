package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entities.Program
import com.zktony.android.ui.utils.PageType
import com.zktony.android.ui.utils.UiFlags
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
     * 下盘计数
     */
    private val _count = MutableStateFlow(0)

    /**
     * 记录上盘运动位置，改变ui
     */
    private val _spCount = MutableStateFlow(0)

    private val _job = MutableStateFlow<Job?>(null)
    private var syringeJob: Job? = null


    val uiState = _uiState.asStateFlow()

    val count = _count.asStateFlow()

    val spCount = _spCount.asStateFlow()

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
     * 举升1上盘高度
     */
    val spgd = dataSaver.readData("spgd", 0f);


    /**
     * 举升2复位高度
     */
    val fwgd2 = dataSaver.readData("fwgd2", 0f);

    /**
     * 举升2盘子距离
     */
    val pzgd2 = dataSaver.readData("pzjl2", 0f);

    /**
     * 举升2上盘高度
     */
    val spgd2 = dataSaver.readData("spgd2", 0f);


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

    val tiji = dataSaver.readData("tiji", 0f)


    /**
     * 上盘运动次数
     * 默认上培养皿运动了次数开始
     */
    var spStartNum = 0

    /**
     * 下培养皿的运动次数
     */
    var xpStartNum = 1


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
            is HomeEvent.spStart -> spStart(event.index)
            is HomeEvent.xpStart -> xpStart()
            is HomeEvent.PumpingOrRecrement -> PumpingOrRecrement(event.index)
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
                                    ads = Triple(50L, 80L, 100L)
                                }

                            }
                        }

                        // 进行正向运动
                        tx {
                            timeout = 1000L * 10
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 800L
                                ads = Triple(50L, 80L, 100L)
                            }
                        }

                        // 进行反向运动
                        tx {
                            timeout = 1000L * 15
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 3200L * -3
                                ads = Triple(50L, 80L, 100L)
                            }
                        }
                    }

                    //移动上盘到原点距离
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 5
                            pulse = (3200L * spydjl).toLong();
                            ads = Triple(50L, 80L, 100L)
                        }

                        //移动下盘到原点距离
                        move(MoveType.MOVE_PULSE) {
                            index = 4
                            pulse = (2599L * xpydjl).toLong();
                            ads = Triple(50L, 80L, 100L)
                        }

                    }

                    tx {
                        //移动到复位高度
                        move(MoveType.MOVE_PULSE) {
                            index = 1
                            pulse = (3200L * fwgd).toLong();
                            ads = Triple(50L, 80L, 100L)
                        }
                        //移动到复位高度
                        move(MoveType.MOVE_PULSE) {
                            index = 0
                            pulse = (3200L * fwgd2).toLong();
                            ads = Triple(50L, 80L, 100L)
                        }
                    }
                    xpStartNum = 1
                }
            } catch (ex: Exception) {
                _loading.value = 0
            } finally {
                _loading.value = 0
            }
        }
    }

    /**
     * 上培养皿运动
     */
    private fun spStart(runIndex: Int) {
        viewModelScope.launch {

            /**
             * 1.获取移动步数
             */
            var spydjl = dataSaver.readData("spydjl", 0f)
            var spydbs = (spydjl * 3200).toLong()
            spydbs += runIndex * 1666

            /**
             * 1.举升1到上盘高度
             *
             * 2.上盘移动400步
             *
             * 3.1举升1到复位高度
             * 3.2举升2到上盘高度
             *
             * 4.上盘移动剩余的1266
             *
             * 5.举升2到复位高度
             *
             */
            tx {
                move(MoveType.MOVE_PULSE) {
                    index = 1
                    pulse = (spgd * 3200L).toLong()
                }
            }

            tx {
                move(MoveType.MOVE_PULSE) {
                    index = 5
                    pulse = spydbs - 1266
                }
            }

            tx {
                move(MoveType.MOVE_PULSE) {
                    index = 1
                    pulse = (fwgd * 3200L).toLong()
                }
                move(MoveType.MOVE_PULSE) {
                    index = 0
                    pulse = (spgd2 * 3200L).toLong()
                }

            }

            tx {
                move(MoveType.MOVE_PULSE) {
                    index = 5
                    pulse = spydbs
                }
            }

            tx {
                move(MoveType.MOVE_PULSE) {
                    index = 0
                    pulse = (fwgd2 * 3200L).toLong()
                }

            }


        }

    }

    /**
     * 下培养皿运动
     */
    private fun xpStart() {
        viewModelScope.launch {

            if (xpStartNum < 8) {
                /**
                 * 1.获取移动步数
                 */
                var spydjl = dataSaver.readData("spydjl", 0f)
                var spydbs = (spydjl * 3200).toLong()
                spydbs += (spStartNum + xpStartNum) * 1666

                /**
                 * 1.举升1到上盘高度
                 *
                 * 2.上盘移动400步
                 *
                 * 3.1举升1到复位高度
                 * 3.2举升2到上盘高度
                 *
                 * 4.上盘移动剩余的1266
                 *
                 * 5.举升2到复位高度
                 *
                 */
                tx {
                    move(MoveType.MOVE_PULSE) {
                        index = 1
                        pulse = (spgd * 3200L).toLong()
                    }
                }

                tx {
                    move(MoveType.MOVE_PULSE) {
                        index = 5
                        pulse = spydbs - 1266
                    }
                }

                tx {
                    move(MoveType.MOVE_PULSE) {
                        index = 1
                        pulse = (fwgd * 3200L).toLong()
                    }
                    move(MoveType.MOVE_PULSE) {
                        index = 0
                        pulse = (spgd2 * 3200L).toLong()
                    }

                }

                tx {
                    move(MoveType.MOVE_PULSE) {
                        index = 5
                        pulse = spydbs
                    }
                }

                tx {
                    move(MoveType.MOVE_PULSE) {
                        index = 0
                        pulse = (fwgd2 * 3200L).toLong()
                    }

                }

                if (_spCount.value == 7) {
                    _spCount.value = 0
                } else {
                    _spCount.value += 1
                }
                xpStartNum += 1
            }




        }


    }


    private fun PumpingOrRecrement(int: Int) {

        viewModelScope.launch {
            /**
             * 4=排液
             * 5=回吸
             * 3=停止
             */
            _loading.value = int

            if (_loading.value == 3 || _loading.value == 6) {
                tx { stop(3) }
                _loading.value = 0
            }

            while (_loading.value == 4 || _loading.value == 5) {
                if (_loading.value == 4) {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 3
                            pulse = 32000L
                        }

                    }
                } else {
                    tx {
                        move(MoveType.MOVE_PULSE) {
                            index = 3
                            pulse = -32000L
                        }

                    }
                }

            }

        }
    }


    /**
     * Starts the execution of the selected program entity.
     */
    private fun start(int: Int) {
        viewModelScope.launch {
            _loading.value = int
            if (_loading.value != 8) {
                /**
                 * 是否有培养皿运动过
                 */
                var isStart = false


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
                 * 下盘原点距离
                 */
                var xpydjl = dataSaver.readData("xpydjl", 0f);
                xpydjl = xpydjl * 2599

                /**
                 * 下盘孔位间距3255步
                 */
                var xpkwjj = 2599

                /**
                 * 检测正确的培养皿个数,坐标从2到0
                 */
                var jiance2PYM = 0


                /**
                 * 上培养皿运动了几次
                 */
                var valveOne = dataSaver.readData("valveOne", 0)


                spStartNum = valveOne

                _spCount.value = valveOne


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

                        }
                        tx {
                            //3.夹爪夹紧
                            move(MoveType.MOVE_PULSE) {
                                index = 2
                                pulse = (3200L * jjjl).toLong();
                            }
                        }

                        tx {
                            queryGpio(7)
                            delay = 300L
                        }

                        /**
                         * 获取是否有培养皿
                         * true=有
                         * false=没有
                         */
                        val jiance1 = getGpio(7)
//                        val jiance1 = false
                        if (jiance1) {
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
                        tx {
                            //1.夹爪松开
                            move(MoveType.MOVE_PULSE) {
                                index = 2
                                pulse = (3200L * skjl).toLong()
                            }

                        }

                        tx {
                            //2.举升1移动到分离距离
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = (3200L * fljl).toLong()
                            }
                        }

                        tx {
                            //13.夹爪夹紧
                            move(MoveType.MOVE_PULSE) {
                                index = 2
                                pulse = (3200L * jjjl).toLong()
                            }

                        }

                        tx {
                            //4.举升1移动到0
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = 0
                            }
                        }
                        tx {
                            /**
                             * 检测培养皿是否摆放正确
                             */
                            queryGpio(6)
                            delay = 300L
                        }


                        /**
                         * 获取检测培养皿是否摆放正确
                         * false=摆放正确
                         * true=摆放错误
                         */
                        val jiance2 = getGpio(6)
//                        val jiance2 = true
                        if (!jiance2) {
                            startState = 3;
                            continue;
                        } else {
                            startState = 4;
                            continue;
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
                                //夹爪松开
                                move(MoveType.MOVE_PULSE) {
                                    index = 2
                                    pulse = (3200L * skjl).toLong();
                                }
                            }

                            tx {
                                //举升1运动到复位高度
                                move(MoveType.MOVE_PULSE) {
                                    index = 1
                                    pulse = (3200L * fwgd).toLong();
                                }

                                //举升2运动到复位高度
                                move(MoveType.MOVE_PULSE) {
                                    index = 0
                                    pulse = (3200L * fwgd2).toLong();
                                }

                            }
                            if (spStartCoordinates < 7) {
                                /**
                                 * 上盘原点距离
                                 */
                                val spydjl =
                                    dataSaver.readData("spydjl", 0f);
                                spStartNum += 1
                                if (_spCount.value == 7) {
                                    _spCount.value = 0
                                } else {
                                    _spCount.value += 1
                                }
                                println("上盘运动步数===" + spydjl * 3200 + spStartNum * 1666)
                                tx {
                                    //上盘移动1格
                                    move(MoveType.MOVE_PULSE) {
                                        index = 5
                                        pulse = (spydjl * 3200 + spStartNum * 1666).toLong();
                                    }
                                }
                            }

                            if (spStartCoordinates == 7) {
                                _loading.value = 2
                                break;
                            } else {
                                spStartCoordinates += 1;
                            }
                            startState = 0;
                            continue;


                        } else {
                            //不是第一次运动,一罐培养皿已经运行完成，做下一灌培养皿的运动准备

                            tx {

                                //夹爪松开
                                move(MoveType.MOVE_PULSE) {
                                    index = 2
                                    pulse = (3200L * skjl).toLong();
                                }
                            }

                            tx {
                                //举升1移动到0
                                move(MoveType.MOVE_PULSE) {
                                    index = 1
                                    pulse = 0
                                }
                            }

                            var i = 0;
                            if (jiance2PYM == 0) {
                                xpkwjj += 2599
                            }
                            while (i <= jiance2PYM) {
                                tx {
                                    /**
                                     * 下盘移动一格
                                     */
                                    move(MoveType.MOVE_PULSE) {
                                        index = 4
                                        pulse = (xpydjl + xpkwjj).toLong();
                                    }
                                }

                                tx {
                                    /**
                                     * 举升2举升到盘子高度
                                     */
                                    move(MoveType.MOVE_PULSE) {
                                        index = 0
                                        pulse = (3200L * pzgd2).toLong();
                                    }
                                }

                                tx {
                                    /**
                                     * 举升2举升到0
                                     */
                                    move(MoveType.MOVE_PULSE) {
                                        index = 0
                                        pulse = 0
                                    }
                                }


                                xpkwjj += 2599
                                i++
                            }


                            /**
                             * 下盘复位
                             */
                            tx {
                                timeout = 1000L * 60
                                move(MoveType.MOVE_PULSE) {
                                    index = 4
                                    pulse = 3200L * -30
                                    ads = Triple(50L, 80L, 100L)
                                }

                            }
                            // 进行正向运动
                            tx {
                                timeout = 1000L * 10
                                move(MoveType.MOVE_PULSE) {
                                    index = 4
                                    pulse = 800L
                                    ads = Triple(50L, 80L, 100L)
                                }
                            }

                            // 进行反向运动
                            tx {
                                timeout = 1000L * 15
                                move(MoveType.MOVE_PULSE) {
                                    index = 4
                                    pulse = 3200L * -3
                                    ads = Triple(50L, 80L, 100L)
                                }
                            }

                            tx {
                                //移动下盘到原点距离
                                move(MoveType.MOVE_PULSE) {
                                    index = 4
                                    pulse = xpydjl.toLong();
                                    ads = Triple(50L, 80L, 100L)
                                }
                            }
                            xpkwjj = 2599
                            /**
                             * 下盘复位
                             */


                            tx {
                                /**
                                 * 举升2举升到复位高度，让上盘运动
                                 */
                                move(MoveType.MOVE_PULSE) {
                                    index = 0
                                    pulse = (3200L * fwgd2).toLong();
                                }

                                /**
                                 * 举升1举升到复位高度，让上盘运动
                                 */
                                move(MoveType.MOVE_PULSE) {
                                    index = 1
                                    pulse = (3200L * fwgd).toLong();
                                }
                            }


                            if (spStartCoordinates < 7) {
                                /**
                                 * 上盘原点距离
                                 */
                                val spydjl =
                                    dataSaver.readData("spydjl", 0f);
                                spStartNum += 1
                                if (_spCount.value == 7) {
                                    _spCount.value = 0
                                } else {
                                    _spCount.value += 1
                                }
                                println("上盘运动步数===" + spydjl * 3200 + spStartNum * 1666)
                                tx {
                                    //上盘移动1格
                                    move(MoveType.MOVE_PULSE) {
                                        index = 5
                                        pulse = (spydjl * 3200 + spStartNum * 1666).toLong();
                                    }
                                }

                            }

                            if (_loading.value == 8) {
                                _loading.value = 2
                                break;
                            }

                            if (spStartCoordinates == 7) {
                                _loading.value = 2
                                break;
                            }
                            spStartCoordinates += 1;
                            isStart = false
                            startState = 0;

                            continue;
                        }


                    } else if (startState == 3) {
                        /**
                         * 培养皿摆放正确的运动
                         */
                        if (!isStart) {
                            jiance2PYM = 0
                        } else {
                            jiance2PYM = 1
                        }


                        /**
                         * 第一次的运动
                         */
                        isStart = true

                        tx {
                            /**
                             * 下盘移动一格
                             */
                            move(MoveType.MOVE_PULSE) {
                                index = 4
                                pulse = (xpydjl + xpkwjj).toLong();
                            }
                        }

                        tx {

                            /**
                             * 加液
                             */
                            move(MoveType.MOVE_DV) {
                                index = 3
                                dv = tiji
                            }

                            /**
                             * 举升1举升到夹爪高度
                             */
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = (3200L * jzgd).toLong();
                            }

                            /**
                             * 举升2举升到盘子高度
                             */
                            move(MoveType.MOVE_PULSE) {
                                index = 0
                                pulse = (3200L * pzgd2).toLong();
                            }

                        }
                        xpkwjj += 2599
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

                                //夹爪松开
                                move(MoveType.MOVE_PULSE) {
                                    index = 2
                                    pulse = (3200L * skjl).toLong()
                                }

                                /**
                                 * 举升1举升到矫正高度
                                 */
                                move(MoveType.MOVE_PULSE) {
                                    index = 1
                                    pulse = (3200L * jiaozgd).toLong();
                                }


                            }

                            tx {
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
                            }

                            tx {
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
                            displayError += 1

                        }
                        _loading.value = 2
                        break;
                    }
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
    val spCount: Int = 0,
    val uiFlags: Int = UiFlags.NONE
)

/**
 * Sealed class for the events of the Home screen.
 */
sealed class HomeEvent {
    data object Reset : HomeEvent()
    data class Start(val index: Int) : HomeEvent()
    data class spStart(val index: Int) : HomeEvent()
    data object xpStart : HomeEvent()
    data class PumpingOrRecrement(val index: Int) : HomeEvent()
    data object Stop : HomeEvent()
    data class NavTo(val page: PageType) : HomeEvent()
    data class ToggleSelected(val id: Long) : HomeEvent()
    data class Clean(val index: Int) : HomeEvent()
    data class Syringe(val index: Int) : HomeEvent()
    data class Pipeline(val index: Int) : HomeEvent()
}