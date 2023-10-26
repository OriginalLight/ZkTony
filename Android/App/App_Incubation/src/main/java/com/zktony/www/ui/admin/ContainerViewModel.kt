package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.core.ext.execute
import com.zktony.www.data.dao.ContainerDao
import com.zktony.www.data.entities.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContainerViewModel constructor(
    private val CD: ContainerDao
) : BaseViewModel() {

    private val _container: MutableStateFlow<Container> = MutableStateFlow(Container())
    val container = _container.asStateFlow()

    init {
        viewModelScope.launch {
            CD.getAll().collect {
                if (it.isNotEmpty()) {
                    _container.value = it.first()
                }
            }
        }
    }

    /**
     * 更新容器
     * @param container [Container] 容器
     */
    fun update(container: Container) {
        viewModelScope.launch {
            CD.insert(container)
        }
    }

    fun mveToY(y: Float) {
        viewModelScope.launch {
            execute {
                dv {
                    this.y = y
                }
            }
        }
    }

    fun mveToZ(y: Float, z: Float) {
        viewModelScope.launch {
            execute {
                dv {
                    this.y = y
                }
                dv {
                    this.y = y
                    this.z = z
                }
            }
        }
    }
}