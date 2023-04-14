package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.*

class ContainerManager constructor(
    private val CD: ContainerDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            CD.getByType(0).collect {
                if (it.isEmpty()) {
                    CD.insert(
                        Container(
                            name = "废液槽"
                        )
                    )
                }
            }
        }
    }

    fun initializer() {
        "容器管理器初始化完成！！！".logi()
    }
}