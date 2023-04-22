package com.zktony.www.manager

import com.zktony.core.ext.Ext
import com.zktony.core.ext.logi
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.entity.Container
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
                            name = Ext.ctx.getString(com.zktony.core.R.string.waste_tank)
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