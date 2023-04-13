package com.zktony.www.manager

import com.zktony.core.ext.logi
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContainerManager constructor(
    private val containerDao: ContainerDao
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            containerDao.getByType(0).collect {
                if (it.isEmpty()) {
                    containerDao.insert(Container(name = "废液槽"))
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