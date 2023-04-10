package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.manager.ExecutionManager
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ContainerViewModel constructor(
    private val dao: ContainerDao,
    private val executionManager: ExecutionManager
) : BaseViewModel() {

    private val _container: MutableStateFlow<Container> = MutableStateFlow(Container())
    val container = _container.asStateFlow()

    init {
        viewModelScope.launch {
            dao.getAll().collect {
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
            dao.insert(container)
        }
    }

    /**
     * 测试 移动到废液槽
     */
    fun toWasteY() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.wasteY)
            )
        }
    }

    /**
     * 测试 废液槽针头下降
     */
    fun toWasteZ() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.wasteY),
                executionManager.generator(y = container.value.wasteY, z = container.value.wasteZ)
            )
        }
    }


    /**
     * 测试 移动到洗液槽
     */
    fun toWashY() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.washY)
            )
        }
    }

    /**
     * 测试 洗液槽针头下降
     */
    fun toWashZ() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.washY),
                executionManager.generator(y = container.value.washY, z = container.value.washZ)
            )
        }
    }

    /**
     * 测试 移动到阻断液槽
     */
    fun toBlockY() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.blockY)
            )
        }
    }

    /**
     * 测试 阻断液槽针头下降
     */
    fun toBlockZ() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.blockY),
                executionManager.generator(y = container.value.blockY, z = container.value.blockZ)
            )
        }
    }

    /**
     * 测试 移动到抗体一槽
     */
    fun toOneY() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.oneY)
            )
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun toOneZ() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.oneY),
                executionManager.generator(y = container.value.oneY, z = container.value.oneZ)
            )
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun toRecycleOneZ() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.oneY),
                executionManager.generator(
                    y = container.value.oneY,
                    z = container.value.recycleOneZ
                )
            )
        }
    }

    /**
     * 测试 移动到抗体二槽
     */
    fun toTwoY() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.twoY)
            )
        }
    }

    /**
     * 测试 抗体二槽针头下降
     */
    fun toTwoZ() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator(y = container.value.twoY),
                executionManager.generator(y = container.value.twoY, z = container.value.twoZ)
            )
        }
    }

    /**
     * 测试 回到原点
     */
    fun toZero() {
        viewModelScope.launch {
            executionManager.executor(
                executionManager.generator()
            )
        }

    }
}