package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.*

class ContainerManager constructor(
    private val containerDao: ContainerDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            containerDao.getAll().collect {
                if (it.isEmpty()) {
                    containerDao.insert(Container())
                }
            }
        }
    }

    fun init() {
        scope.launch {
            "容器管理器初始化完成！！！".logi()
        }
    }
}