package com.zktony.www.manager

import com.zktony.core.utils.Snowflake
import com.zktony.core.ext.logi
import com.zktony.www.room.dao.ContainerDao
import com.zktony.www.room.dao.HoleDao
import com.zktony.www.room.dao.PlateDao
import com.zktony.www.room.entity.Container
import com.zktony.www.room.entity.Hole
import com.zktony.www.room.entity.Plate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContainerManager constructor(
    private val containerDao: ContainerDao,
    private val plateDao: PlateDao,
    private val holeDao: HoleDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    init {
        scope.launch {
            containerDao.getAll().collect {
                if (it.isEmpty()) {
                    containerDao.insert(
                        Container(
                            id = 1L,
                            name = "默认容器",
                        )
                    )
                    plateDao.insert(
                        Plate(
                            id = 1L,
                            subId = 1L,
                            size = 10,
                        )
                    )
                    val holes = mutableListOf<Hole>()
                    val snowflake = Snowflake(1)
                    for (i in 0 until 10) {
                        holes.add(
                            Hole(
                                id = snowflake.nextId(),
                                subId = 1L,
                                y = i,
                            )
                        )
                    }
                    holeDao.insertAll(holes)
                }
            }
        }
    }

    fun test() {
        scope.launch { "ContainerManager test".logi() }
    }
}