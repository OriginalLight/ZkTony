package com.zktony.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zktony.android.data.dao.ContainerDao
import com.zktony.android.data.entity.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/5/11 14:48
 */
class ContainerViewModel constructor(
    private val dao: ContainerDao
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

    fun navigationTo(page: ContainerPage) {
        _uiState.value = _uiState.value.copy(page = page)
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }

    fun insert(container: Container) {
        viewModelScope.launch {
            dao.insert(container)
        }
    }

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