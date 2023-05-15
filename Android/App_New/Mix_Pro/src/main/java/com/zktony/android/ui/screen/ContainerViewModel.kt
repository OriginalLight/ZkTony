package com.zktony.android.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ContainerDao
import com.zktony.android.data.entity.ContainerEntity
import com.zktony.android.data.entity.Point
import com.zktony.android.ui.navigation.PageEnum
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/11 14:48
 */
class ContainerViewModel constructor(
    private val dao: ContainerDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContainerUiState())
    private val _page = MutableStateFlow(PageEnum.MAIN)
    private val _selected = MutableStateFlow(0L)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                _page,
                _selected,
            ) { entities, page, selectedId ->
                ContainerUiState(entities = entities, page = page, selected = selectedId)
            }.catch { ex ->
                _uiState.value = ContainerUiState(errorMessage = ex.message ?: "Unknown error")
            }.collect {
                _uiState.value = it
            }
        }
    }

    fun navigationTo(page: PageEnum) {
        _page.value = page
    }

    fun toggleSelected(id: Long) {
        _selected.value = id
    }

    fun insert(name: String) {
        viewModelScope.launch {
            val list: MutableList<Point> = mutableListOf()
            for (i in 0 until 6) {
                list.add(Point(index = i))
            }
            dao.insert(ContainerEntity(name = name, data = list))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun update(containerEntity: ContainerEntity) {
        viewModelScope.launch {
            dao.update(containerEntity)
        }
    }
}

data class ContainerUiState(
    val entities: List<ContainerEntity> = emptyList(),
    val page: PageEnum = PageEnum.MAIN,
    val selected: Long = 0L,
    val errorMessage: String = "",
)