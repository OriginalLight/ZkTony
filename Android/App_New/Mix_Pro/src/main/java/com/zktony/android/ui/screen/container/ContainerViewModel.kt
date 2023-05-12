package com.zktony.android.ui.screen.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ContainerDao
import com.zktony.android.data.entity.Container
import com.zktony.android.data.entity.Point
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/11 14:48
 */
class ContainerViewModel constructor(
    private val dao: ContainerDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ContainerUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
                _uiState.value = _uiState.value.copy(list = it)
            }
        }
    }

    /**
     * 页面切换
     *
     * @param page ContainerPage
     * @return Unit
     */
    fun navigationTo(page: ContainerPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    /**
     * 添加
     *
     * @param name String
     * @return Unit
     */
    fun insert(name: String) {
        viewModelScope.launch {
            val list: MutableList<Point> = mutableListOf()
            for (i in 0 until 6) {
                list.add(Point(index = i))
            }
            dao.insert(Container(name = name, data = list))
        }
    }

    /**
     * 删除
     *
     * @param id Long
     * @return Unit
     */
    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    /**
     * 更新
     *
     * @param container Container
     * @return Unit
     */
    fun update(container: Container) {
        viewModelScope.launch {
            dao.update(container)
        }
    }

    /**
     * entityFlow
     *
     * @param id Long
     * @return Flow<Container>
     */
    fun entityFlow(id: Long) = dao.getById(id)
}

data class ContainerUiState(
    val list: List<Container> = emptyList(),
    val page: ContainerPage = ContainerPage.CONTAINER
)

enum class ContainerPage {
    CONTAINER,
    CONTAINER_EDIT,
    CONTAINER_ADD
}