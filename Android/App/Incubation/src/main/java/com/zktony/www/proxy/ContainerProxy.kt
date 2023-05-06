package com.zktony.www.proxy

import com.zktony.core.ext.logi
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.*

class ContainerProxy constructor(
    private val CD: ContainerDao,
) {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            CD.getAll().collect {
                if (it.isEmpty()) {
                    CD.insert(Container())
                }
            }
        }
    }


    fun initializer() {
        "ContainerProxy initializer".logi()
    }
}