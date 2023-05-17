package com.zktony.android.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ContainerDao
import com.zktony.android.data.dao.ProgramDao
import com.zktony.android.data.entity.ContainerEntity
import com.zktony.android.data.entity.Point
import com.zktony.android.data.entity.ProgramEntity
import com.zktony.android.ui.utils.PageEnum
import com.zktony.core.ext.showShortToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/15 14:51
 */
class ProgramViewModel constructor(
    private val dao: ProgramDao,
    private val containerDao: ContainerDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgramUiState())
    private val _page = MutableStateFlow(PageEnum.MAIN)
    private val _selected = MutableStateFlow(0L)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                dao.getAll(),
                containerDao.getAll(),
                _page,
                _selected,
            ) { entities, containers, page, selectedId ->
                ProgramUiState(
                    entities = entities,
                    containers = containers,
                    page = page,
                    selected = selectedId
                )
            }.catch { ex ->
                ex.printStackTrace()
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
            if (uiState.value.containers.isEmpty()) {
                "请先添加容器".showShortToast()
            } else {
                val container = uiState.value.containers[0]
                dao.insert(
                    ProgramEntity(
                        subId = container.id,
                        name = name,
                        data = container.data,
                    )
                )
            }
        }
    }

    fun update(entity: ProgramEntity) {
        viewModelScope.launch {
            dao.update(entity)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun toggleActive(index: Int) {
        viewModelScope.launch {
            val entity = uiState.value.entities.find { it.id == uiState.value.selected }
            entity?.let {
                val data = it.data
                val list = mutableListOf<Point>()
                data.forEach { p ->
                    if (p.index == index) {
                        list.add(p.copy(active = !p.active))
                    } else {
                        list.add(p)
                    }
                }
                dao.update(it.copy(data = list))
            }
        }
    }

    fun toggleContainer(containerEntity: ContainerEntity) {
        viewModelScope.launch {
            val entity = uiState.value.entities.find { it.id == uiState.value.selected }
            entity?.let {
                dao.update(it.copy(subId = containerEntity.id, data = containerEntity.data))
            }
        }
    }
}

data class ProgramUiState(
    val entities: List<ProgramEntity> = emptyList(),
    val containers: List<ContainerEntity> = emptyList(),
    val selected: Long = 0L,
    val page: PageEnum = PageEnum.MAIN,
)