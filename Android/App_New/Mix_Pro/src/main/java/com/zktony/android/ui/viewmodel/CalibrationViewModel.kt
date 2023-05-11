package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.CalibrationDao
import com.zktony.android.data.dao.CalibrationDataDao
import com.zktony.android.data.entity.Calibration
import com.zktony.android.data.entity.CalibrationData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/9 13:19
 */
class CalibrationViewModel constructor(
    private val dao: CalibrationDao,
    private val dataDao: CalibrationDataDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * Navigate to
     *
     * @param page CalibrationPage
     * @return Unit
     */
    fun navigateTo(page: CalibrationPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    /**
     * Insert
     *
     * @param calibration Calibration
     * @return Unit
     */
    fun insert(calibration: Calibration) {
        viewModelScope.launch {
            dao.insert(calibration)
        }
    }

    /**
     * Insert Data
     *
     * @param data CalibrationData
     * @return Unit
     */
    fun insertData(data: CalibrationData) {
        viewModelScope.launch {
            dataDao.insert(data)
            delete(500L)
            calculateActual(data.subId)
        }
    }

    /**
     * Delete
     *
     * @param id Long
     * @return Unit
     */
    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
            dataDao.deleteBySubId(id)
            delay(500L)
            val list = uiState.value.list
            if (list.isNotEmpty() && !list.any { it.active == 1 }) {
                dao.update(list[0].copy(active = 1))
            }
        }
    }

    /**
     * Delete Data
     *
     * @param id Long
     * @param index Int
     * @return Unit
     */
    fun deleteData(id: Long, index: Int) {
        viewModelScope.launch {
            dataDao.deleteByIndex(id, index)
            delete(500L)
            calculateActual(id)
        }
    }

    /**
     * Update
     *
     * @param calibration Calibration
     * @return Unit
     */
    fun update(calibration: Calibration) {
        viewModelScope.launch {
            dao.update(calibration)
        }
    }

    /**
     * 选中校准文件
     *
     * @param id Long
     * @return Unit
     */
    fun enable(id: Long) {
        viewModelScope.launch {
            val list = uiState.value.list
                .map {
                    if (it.id != id) {
                        it.copy(active = 0)
                    } else {
                        it.copy(active = 1)
                    }
                }
            dao.updateAll(list)

        }
    }

    /**
     * 返回校准文件数据列表
     *
     * @param id Long
     * @return Flow<List<CalibrationData>>
     */
    fun dataList(id: Long) = dataDao.getBySubId(id)

    /**
     * Calculate Actual
     *
     * @param id Long
     * @return Unit
     */
    private suspend fun calculateActual(id: Long) {
        val cali = dao.getById(id).firstOrNull()
        val dataList = dataDao.getBySubId(id).firstOrNull()
        val vl = List(13) { 100f }.toMutableList()
        if (!dataList.isNullOrEmpty()) {
            vl.forEachIndexed { index, item ->
                dataList.filter { it.index == index }.let {
                    if (it.isNotEmpty()) {
                        vl[index] = item * it.map { data -> data.percent }.average().toFloat()
                    }
                }

            }
        }
        dao.update(
            cali!!.copy(
                v1 = vl[0], v2 = vl[1], v3 = vl[2],
                v4 = vl[3], v5 = vl[4], v6 = vl[5],
                v7 = vl[6], v8 = vl[7], v9 = vl[8],
                v10 = vl[9], v11 = vl[10], v12 = vl[11], v13 = vl[12]
            )
        )
    }

    /**
     * 加液
     *
     * @param i Int
     * @param fl Float
     * @return Unit
     */
    fun addLiquid(i: Int, fl: Float) {

    }

}

data class CalibrationUiState(
    val list: List<Calibration> = emptyList(),
    val page: CalibrationPage = CalibrationPage.CALIBRATION,
)

enum class CalibrationPage {
    CALIBRATION,
    CALIBRATION_ADD,
    CALIBRATION_EDIT
}