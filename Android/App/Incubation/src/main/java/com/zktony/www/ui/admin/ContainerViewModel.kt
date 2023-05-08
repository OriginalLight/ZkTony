package com.zktony.www.ui.admin

import androidx.lifecycle.viewModelScope
import com.zktony.core.base.BaseViewModel
import com.zktony.www.common.ext.execute
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
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

    /**
     * 测试 移动到废液槽
     */
    fun toWasteY() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.wasteY
                }
            }
        }
    }

    /**
     * 测试 废液槽针头下降
     */
    fun toWasteZ() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.wasteY
                }
                step {
                    y = container.value.wasteY
                    z = container.value.wasteZ
                }
            }
        }
    }


    /**
     * 测试 移动到洗液槽
     */
    fun toWashY() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.washY
                }
            }
        }
    }

    /**
     * 测试 洗液槽针头下降
     */
    fun toWashZ() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.washY
                }
                step {
                    y = container.value.washY
                    z = container.value.washZ
                }
            }
        }
    }

    /**
     * 测试 移动到阻断液槽
     */
    fun toBlockY() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.blockY
                }
            }
        }
    }

    /**
     * 测试 阻断液槽针头下降
     */
    fun toBlockZ() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.blockY
                }
                step {
                    y = container.value.blockY
                    z = container.value.blockZ
                }
            }
        }
    }

    /**
     * 测试 移动到抗体一槽
     */
    fun toOneY() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.oneY
                }
            }
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun toOneZ() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.oneY
                }
                step {
                    y = container.value.oneY
                    z = container.value.oneZ
                }
            }
        }
    }

    /**
     * 测试 抗体一槽针头下降
     */
    fun toRecycleOneZ() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.oneY
                }
                step {
                    y = container.value.oneY
                    z = container.value.recycleOneZ
                }
            }
        }
    }

    /**
     * 测试 移动到抗体二槽
     */
    fun toTwoY() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.twoY
                }
            }
        }
    }

    /**
     * 测试 抗体二槽针头下降
     */
    fun toTwoZ() {
        viewModelScope.launch {
            execute {
                step {
                    y = container.value.twoY
                }
                step {
                    y = container.value.twoY
                    z = container.value.twoZ
                }
            }
        }
    }

    /**
     * 测试 回到原点
     */
    fun toZero() {
        viewModelScope.launch {
            execute {
                step {}
            }
        }
    }
}