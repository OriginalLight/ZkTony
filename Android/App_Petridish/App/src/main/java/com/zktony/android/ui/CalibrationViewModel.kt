package com.zktony.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.entities.Calibration
import com.zktony.android.ui.utils.PageType
import com.zktony.android.utils.tx.MoveType
import com.zktony.android.utils.tx.getGpio
import com.zktony.android.utils.tx.tx
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CalibrationViewModel constructor(private val dao: CalibrationDao) : ViewModel() {

    private val _selected = MutableStateFlow(0L)
    private val _page = MutableStateFlow(PageType.CALIBRATION_LIST)
    private val _loading = MutableStateFlow(false)
    private val _uiState = MutableStateFlow(CalibrationUiState())
    private val _loadingNum = MutableStateFlow(0)

    val uiState = _uiState.asStateFlow()


    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _selected,
                _page,
                _loading,
            ) { entities, selected, page, loading ->
                CalibrationUiState(
                    entities = entities,
                    selected = selected,
                    page = page,
                    loading = loading
                )
            }.catch { ex ->
                ex.printStackTrace()
            }.collect {
                _uiState.value = it
            }
        }
    }

    /**
     * Handles the given calibration event.
     *
     * @param event The calibration event to handle.
     */
    fun event(event: CalibrationEvent) {
        when (event) {
            is CalibrationEvent.NavTo -> _page.value = event.page
            is CalibrationEvent.ToggleSelected -> _selected.value = event.id
            is CalibrationEvent.Insert -> viewModelScope.launch { dao.insert(Calibration(text = event.name)) }
            is CalibrationEvent.Delete -> viewModelScope.launch { dao.deleteById(event.id) }
            is CalibrationEvent.Update -> viewModelScope.launch { dao.update(event.entity) }
            is CalibrationEvent.Active -> viewModelScope.launch { dao.active(event.id) }
            is CalibrationEvent.AddLiquid -> addLiquid(event.index)
            is CalibrationEvent.DeleteData -> deleteData(event.data)
            is CalibrationEvent.InsertData -> insertData(event.index, event.volume)
            is CalibrationEvent.Reset -> reset(
                event.ids,
                event.spydjl,
                event.xpydjl,
                event.fwgd,
                event.fwgd2
            )
        }
    }

    /**
     * Resets the axes and syringe.
     */
    private fun reset(ids: List<Int>, spydjl: Double, xpydjl: Double, fwgd: Double, fwgd2: Double) {
        viewModelScope.launch {
            _loadingNum.value = 1
            try {
                if (!_loading.value) {
                    _loading.value = true
                    // 查询GPIO状态
                    tx { queryGpio(ids) }
                    delay(300L)
                    // 针对每个电机进行初始化
                    ids.forEach {
                        // 如果电机未初始化，则进行初始化
                        if (it == 3) {
                            if (getGpio(it)) {
                                println("第一次反向运动")
                                // 进行电机初始化
                                tx {
                                    timeout = 1000L * 60
                                    move(MoveType.MOVE_PULSE) {
                                        index = it
                                        pulse = 3200L * -30
                                        
                                    }

                                }
                            }
                        } else {
                            if (!getGpio(it)) {
                                println("第一次反向运动")
                                // 进行电机初始化
                                tx {
                                    timeout = 1000L * 60
                                    move(MoveType.MOVE_PULSE) {
                                        index = it
                                        pulse = 3200L * -30
                                        
                                    }

                                }
                            }
                        }

                        println("正向运动")
                        // 进行正向运动
                        tx {
                            timeout = 1000L * 10
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 800L
                                
                            }
                        }

                        println("反向运动")
                        // 进行反向运动
                        tx {
                            timeout = 1000L * 15
                            move(MoveType.MOVE_PULSE) {
                                index = it
                                pulse = 3200L * -3
                                
                            }
                        }
                    }
                    if (ids.size > 1) {
                        //移动上盘到原点距离
                        tx {
                            move(MoveType.MOVE_PULSE) {
                                index = 3
                                pulse = (3200L * spydjl).toLong();
                                
                            }

                            //移动下盘到原点距离
                            move(MoveType.MOVE_PULSE) {
                                index = 4
                                pulse = (1300 * xpydjl).toLong();
                                
                            }

                        }

                        tx {
                            //移动到复位高度
                            move(MoveType.MOVE_PULSE) {
                                index = 1
                                pulse = (3200L * fwgd).toLong();
                                
                            }
                            //移动到复位高度
                            move(MoveType.MOVE_PULSE) {
                                index = 0
                                pulse = (3200L * fwgd2).toLong();
                                
                            }
                        }

                    }

                    _loading.value = false
                }


            } catch (ex: Exception) {
                _loadingNum.value = 0
            } finally {
                _loadingNum.value = 0
            }
        }
    }


    /**
     * Adds a new liquid to the selected calibration entity.
     *
     * @param index The index of the calibration entity to add the liquid to.
     */
    private fun addLiquid(index: Int) {
        viewModelScope.launch {
            // Set the loading state to true
            _loading.value = true

            // Open the valve if the index is 0
            if (index == 0) {
                tx {
                    delay = 100L
                    valve(2 to 1)
                }
            }

            // Add the new liquid to the calibration entity
            tx {
                move(MoveType.MOVE_PULSE) {
                    this.index = index + 2
                    pulse = 3200L * 20
                }
            }

            if (index == 0) {
                tx {
                    delay = 100L
                    valve(2 to 0)
                }
                tx {
                    move(MoveType.MOVE_PULSE) {
                        this.index = 2
                        pulse = 3200L * 20 * -1
                    }
                }
            }

            // Set the loading state to false
            _loading.value = false
        }
    }

    /**
     * Deletes the given calibration data point from the selected calibration entity.
     *
     * @param data The calibration data point to delete.
     */
    private fun deleteData(data: Triple<Int, Double, Double>) {
        viewModelScope.launch {
            // Find the selected calibration entity
            val entity = _uiState.value.entities.find { it.id == _uiState.value.selected }

            // If the selected calibration entity exists, update it by removing the data point
            entity?.let {
                val updatedEntity = it.copy(data = it.data - data)
                dao.update(updatedEntity)
            }
        }
    }

    /**
     * Inserts a new calibration data point to the selected calibration entity.
     *
     * @param index The index of the calibration entity to insert the data point to.
     * @param volume The volume of the new calibration data point.
     */
    private fun insertData(index: Int, volume: Double) {
        viewModelScope.launch {
            // Find the selected calibration entity
            val entity = _uiState.value.entities.find { it.id == _uiState.value.selected }

            // If the selected calibration entity exists, update it with the new data point
            entity?.let {
                val updatedEntity = it.copy(
                    data = it.data + Triple(index, volume, 3200 * 20.0)
                )
                dao.update(updatedEntity)
            }
        }
    }
}

/**
 * The UI state for the calibration screen.
 *
 * @param entities The list of calibration entities.
 * @param selected The ID of the selected calibration entity.
 * @param page The current page type.
 * @param loading Whether the screen is currently loading.
 */
data class CalibrationUiState(
    val entities: List<Calibration> = emptyList(),
    val selected: Long = 0L,
    val page: PageType = PageType.CALIBRATION_LIST,
    val loading: Boolean = false,
)

/**
 * Represents an event that can occur on the calibration screen.
 */
sealed class CalibrationEvent {
    data class NavTo(val page: PageType) : CalibrationEvent()
    data class ToggleSelected(val id: Long) : CalibrationEvent()
    data class Insert(val name: String) : CalibrationEvent()
    data class Delete(val id: Long) : CalibrationEvent()
    data class Update(val entity: Calibration) : CalibrationEvent()
    data class Active(val id: Long) : CalibrationEvent()
    data class AddLiquid(val index: Int) : CalibrationEvent()
    data class DeleteData(val data: Triple<Int, Double, Double>) : CalibrationEvent()
    data class InsertData(val index: Int, val volume: Double) : CalibrationEvent()
    data class Reset(
        val ids: List<Int>,
        val spydjl: Double,
        val xpydjl: Double,
        val fwgd: Double,
        val fwgd2: Double
    ) : CalibrationEvent()
}