package com.zktony.www.manager

import com.zktony.common.utils.logi
import com.zktony.www.data.local.dao.ContainerDao
import com.zktony.www.data.local.entity.Container
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContainerManager constructor(
    private val containerDao: ContainerDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    init {
        scope.launch {
            containerDao.getAll().collect {
                if (it.isEmpty()) {
                    containerDao.insert(Container())
                }
            }
        }
    }

    fun test() {
        scope.launch { "ContainerManager test".logi() }
    }
}